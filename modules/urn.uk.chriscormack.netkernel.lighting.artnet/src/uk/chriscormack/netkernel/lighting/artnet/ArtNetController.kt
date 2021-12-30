package uk.chriscormack.netkernel.lighting.artnet

import ch.bildspur.artnet.ArtNetClient
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class ArtNetController(val universe: Int, val subnet: Int) {
    internal val fadeTickMs = 10

    private val artnet = ArtNetClient()

    private val channelChangeChannels: Map<Int, Channel<ChannelUpdatePayload>>

    internal val transmissionNeeded = Channel<Unit>(Channel.Factory.CONFLATED)

    internal val currentValues = ConcurrentHashMap<Int, Short>()

    private var previousSentDmxData = ByteArray(512)

    private val listeners = ArrayList<IChannelChangeListener>()

    init {
        artnet.start()

        channelChangeChannels = HashMap()

        GlobalScope.launch {
            runTransmissionChannel()

            (1..512).forEach { channelNo ->
                val channel = Channel<ChannelUpdatePayload>(Channel.Factory.CONFLATED)
                channelChangeChannels[channelNo] = channel
                runChannelChangerChannel(channelNo, channel)
            }
        }
    }

    class ChannelUpdatePayload(val change: ChannelChange, val updateNotificationChannel: Channel<Unit>)

    fun setValues(valuesToSet: List<Pair<Int, ChannelChange>>) {
        var valuesChanged = false

        runBlocking {
            valuesToSet.forEach { (channelNo, channelChange) ->
                if (doSetChannel(channelNo, channelChange)) {
                    valuesChanged = true
                }
            }
        }

        if (valuesChanged) {
            runBlocking {
                transmissionNeeded.send(Unit)
            }
        }
    }

    fun setValue(channelNo: Int, channelChange: ChannelChange) {
        val hasUpdated = runBlocking {
            doSetChannel(channelNo, channelChange)
        }

        if (hasUpdated) {
            runBlocking {
                transmissionNeeded.send(Unit)
            }
        }
    }

    fun setValue(channelNo: Int, channelValue: Int, fadeMs: Long = 0) {
        setValue(channelNo, ChannelChange(channelValue, fadeMs))
    }

    private fun CoroutineScope.doSetChannel(channelNo: Int, channelChange: ChannelChange): Boolean {
        if (channelNo < 1 || channelNo > 512) {
            return false
        }
        if (channelChange.newValue < 0 || channelChange.newValue > 255) {
            return false
        }

        val changeChannel = channelChangeChannels[channelNo]
        checkNotNull(changeChannel)

        val updateDoneChannel = Channel<Unit>()

        launch {
            changeChannel.send(ChannelUpdatePayload(channelChange, updateDoneChannel))
            updateDoneChannel.receive()
        }

        return true
    }

    fun getValue(channelNo: Int): Int {
        return currentValues[channelNo]?.toInt() ?: 0
    }

    fun registerListener(listener: IChannelChangeListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun unregisterListener(listener: IChannelChangeListener) {
        listeners.remove(listener)
    }

    fun close() {
        transmissionNeeded.close()
    }

    private fun CoroutineScope.runTransmissionChannel() {
        var isClosed = false

        sendCurrentValues()

        val ticker = ticker(25)

        var consecutiveErrors = 0

        launch(newSingleThreadContext("ArtNetThread")) {
            while(coroutineContext.isActive && !isClosed) {
                try {
                    select<Unit> {
                        ticker.onReceiveCatching {
                            if (it.isClosed) {
                                return@onReceiveCatching
                            }

                            if (transmissionNeeded.receiveCatching().isClosed) {
                                isClosed = true
                                ticker.cancel()
                                return@onReceiveCatching
                            }

                            sendCurrentValues()
                        }
                    }
                    consecutiveErrors = 0
                } catch (e: Exception) {
                    if (consecutiveErrors == 0) {
                        e.printStackTrace()
                    }
                    consecutiveErrors++

                    if (consecutiveErrors > 20) {
                        // if too many errors, we'll bail out and let this thing stop. A restart of NK is needed.
                        println("Too many consecutive errors")
                        throw e
                    }
                    delay(25)
                }
            }

            artnet.stop()
        }
    }

    private fun CoroutineScope.runChannelChangerChannel(channelNo: Int, channel: Channel<ChannelUpdatePayload>) {
        var isClosed = false

        launch(Dispatchers.Default) {
            var tickerState: TickerState? = null

            while (isActive && !isClosed) {
                select<Unit> {
                    channel.onReceiveCatching {
                        if (it.isClosed) {
                            isClosed = true
                            return@onReceiveCatching
                        }

                        tickerState?.ticker?.cancel()
                        tickerState = null

                        val result = it.getOrThrow()

                        val numberOfSteps = if (result.change.fadeMs == 0L) {
                            1
                        } else {
                            max(1, (result.change.fadeMs / fadeTickMs).toInt())
                        }

                        if (numberOfSteps > 1) {
                            tickerState = TickerState(this@ArtNetController, coroutineContext, channelNo, numberOfSteps, result)
                            if (tickerState!!.setValue(0)) {
                                tickerState = null
                            }
                        } else {
                            currentValues[channelNo] = result.change.newValue.toShort()
                        }

                        result.updateNotificationChannel.send(Unit)
                    }

                    if (tickerState != null) {
                        tickerState!!.ticker.onReceive {
                            if (tickerState!!.setValue()) {
                                tickerState = null
                            }
                        }

                    }
                }
            }
        }
    }

    private fun sendCurrentValues() {
        val changes = HashMap<Int, Int>()
        val dmxData = ByteArray(512)

        currentValues.forEach { (channelNo, channelValue) ->
            val byteValue = channelValue.toByte()
            dmxData[channelNo - 1] = byteValue

            if (previousSentDmxData[channelNo - 1] != byteValue) {
                changes[channelNo] = channelValue.toInt()
            }
        }
        previousSentDmxData = dmxData

        artnet.broadcastDmx(universe, subnet, dmxData)

        if (changes.isNotEmpty()) {
            listeners.forEach {
                it.channelsChanged(changes)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArtNetController

        if (universe != other.universe) return false
        if (subnet != other.subnet) return false

        return true
    }

    override fun hashCode(): Int {
        var result = universe
        result = 31 * result + subnet
        return result
    }
}
