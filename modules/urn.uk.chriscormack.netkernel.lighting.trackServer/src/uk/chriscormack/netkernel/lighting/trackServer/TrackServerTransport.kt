package uk.chriscormack.netkernel.lighting.trackServer

import com.google.protobuf.Empty
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.context.RequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinTransport
import org.netkernel.mod.hds.IHDSDocument

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class TrackServerTransport : KotlinTransport() {
    private lateinit var server: Server

    private var listeners = ArrayList<TrackChangeListener>()

    private var lastReceivedTrackDetails: IHDSDocument? = null

    companion object {
        lateinit var INSTANCE: TrackServerTransport
    }

    private val stopChannel = Channel<Channel<Unit>>()
    private val registerChannel = Channel<Pair<Channel<Unit>, TrackChangeListener>>()
    private val unregisterChannel = Channel<TrackChangeListener>()
    private val trackChangedChannel = Channel<TrackDetails>()
    private val trackServerConnectedChannel = Channel<StreamObserver<TrackState>>()
    private val sendPlayerStateChannel = Channel<PlayerState>()

    override fun RequestContext.postCommission() {
        INSTANCE = this@TrackServerTransport

        val port = 50051
        server = ServerBuilder.forPort(port)
                .addService(TrackNotifyImpl())
                .build()
                .start()

        runListenerLoop()
    }

    override fun RequestContext.preDecommission() {
        runBlocking {
            val responseChannel = Channel<Unit>()
            stopChannel.send(responseChannel)

            responseChannel.receive()
        }
    }

    internal fun registerListener(listener: TrackChangeListener) {
        runBlocking {
            val responseChannel = Channel<Unit>()
            registerChannel.send(Pair(responseChannel, listener))

            responseChannel.receive()
        }
    }

    internal fun unregisterListener(listener: TrackChangeListener) {
        runBlocking {
            try {
                unregisterChannel.send(listener)
            } catch (e: ClosedSendChannelException) {
            }
        }
    }

    internal fun play() {
        runBlocking {
            sendPlayerStateChannel.send(PlayerState.PLAYING)
        }
    }

    internal fun pause() {
        runBlocking {
            sendPlayerStateChannel.send(PlayerState.PAUSED)
        }
    }

    private fun runListenerLoop() {
        var isClosed = false

        val tickerChannel = ticker(5000)

        val connectedTrackServers = ArrayList<StreamObserver<TrackState>>()

        GlobalScope.launch(Dispatchers.Default) {
            while (coroutineContext.isActive && !isClosed) {
                try {
                    select<Unit> {
                        stopChannel.onReceive { resultChannel ->
                            try {
                                isClosed = true

                                server.shutdown()

                                registerChannel.close()
                                unregisterChannel.close()
                                trackChangedChannel.close()
                                trackServerConnectedChannel.close()
                                sendPlayerStateChannel.close()
                            } finally {
                                resultChannel.send(Unit)
                            }
                        }

                        registerChannel.onReceive { (resultChannel, listener) ->
                            try {
                                if (!listeners.contains(listener)) {
                                    listeners.add(listener)

                                    val trackDetails = lastReceivedTrackDetails
                                    if (trackDetails != null) {
                                        listener.trackChanged(transportContext, trackDetails)
                                    }
                                }
                            } finally {
                                resultChannel.send(Unit)
                            }
                        }

                        unregisterChannel.onReceive { listener ->
                            listeners.remove(listener)
                        }

                        trackChangedChannel.onReceive { request ->
                            val trackDetails = hds {
                                node("track") {
                                    node("artist", request.artist)
                                    node("title", request.title)
                                    node("playerState") {
                                        node("name", request.playerState.name)
                                        node("ordinal", request.playerState.ordinal)
                                    }
                                }
                            }

                            lastReceivedTrackDetails = trackDetails

                            listeners.forEach {
                                it.trackChanged(transportContext, trackDetails)
                            }
                        }

                        trackServerConnectedChannel.onReceive { responseObserver ->
                            connectedTrackServers += responseObserver

                            responseObserver.onNext(TrackState.newBuilder().setPlayerState(PlayerState.HANDSHAKE).build())
                        }

                        sendPlayerStateChannel.onReceive { playerState ->
                            connectedTrackServers.forEach { responseObserver ->
                                try {
                                    responseObserver.onNext(TrackState.newBuilder().setPlayerState(playerState).build())
                                } catch (e: Exception) {
                                    connectedTrackServers.remove(responseObserver)
                                    responseObserver.onCompleted()
                                }
                            }
                        }

                        tickerChannel.onReceive {
                            connectedTrackServers.forEach { responseObserver ->
                                try {
                                    responseObserver.onNext(TrackState.newBuilder().setPlayerState(PlayerState.PING).build())
                                } catch (e: Exception) {
                                    connectedTrackServers.remove(responseObserver)
                                    responseObserver.onCompleted()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    inner class TrackNotifyImpl : TrackNotifyGrpc.TrackNotifyImplBase() {
        override fun notifyCurrentTrack(request: TrackDetails, responseObserver: StreamObserver<Empty>) {
            runBlocking {
                trackChangedChannel.send(request)
            }

            responseObserver.onNext(Empty.getDefaultInstance())
            responseObserver.onCompleted()
        }

        override fun playerStateNotifier(request: Empty, responseObserver: StreamObserver<TrackState>) {
            runBlocking {
                trackServerConnectedChannel.send(responseObserver)
            }
        }
    }
}
