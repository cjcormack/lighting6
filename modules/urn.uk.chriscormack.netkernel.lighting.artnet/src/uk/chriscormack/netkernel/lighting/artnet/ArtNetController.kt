package uk.chriscormack.netkernel.lighting.artnet

import ch.bildspur.artnet.ArtNetClient
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

data class ChannelChange(val newValue: Int, val fadeMs: Long)

@ObsoleteCoroutinesApi
class ArtNetController(val universe: Int, val subnet: Int) {
    private val artnet = ArtNetClient()

    private val channelChangeChannels: Map<Int, Channel<ChannelUpdatePayload>>

    private val transmissionNeeded = Channel<Unit>(Channel.Factory.CONFLATED)

    private val currentValues = ConcurrentHashMap<Int, Short>()

    private var previousSentDmxData = ByteArray(512)

    private val listeners = ArrayList<IChannelChangeListener>()

    init {
        artnet.start()

        channelChangeChannels = HashMap()

        GlobalScope.launch {
            run(this)

            (1..512).forEach { channelNo ->
                val channel = Channel<ChannelUpdatePayload>(Channel.Factory.CONFLATED)
                channelChangeChannels[channelNo] = channel
                runChannelChanger(this, channelNo, channel)
            }
        }
    }

    class ChannelUpdatePayload(val change: ChannelChange, val updateNotificationChannel: Channel<Unit>)

    fun setValues(valuesToSet: List<Pair<Int, ChannelChange>>) {
        var valuesChanged = false

        runBlocking {
            valuesToSet.forEach {
                if (validateChannelUpdate(it.first, it.second)) {
                    valuesChanged = true

                    val changeChannel = channelChangeChannels[it.first]
                    checkNotNull(changeChannel)

                    val updateDoneChannel = Channel<Unit>()

                    launch {
                        changeChannel.send(ChannelUpdatePayload(it.second, updateDoneChannel))
                        updateDoneChannel.receive()
                    }
                }
            }
        }

        if (valuesChanged) {
            runBlocking {
                transmissionNeeded.send(Unit)
            }
        }
    }

    fun setValue(channelNo: Int, channelValue: Int) {
        if (!doSetValue(channelNo, channelValue)) return

        runBlocking {
            transmissionNeeded.send(Unit)
        }
    }

    private fun validateChannelUpdate(channelNo: Int, update: ChannelChange): Boolean {
        if (channelNo < 1 || channelNo > 512) {
            return false
        }
        if (update.newValue < 0 || update.newValue > 255) {
            return false
        }

        return true
    }

    private fun doSetValue(channelNo: Int, channelValue: Int): Boolean {
        if (channelNo < 1 || channelNo > 512) {
            return false
        }
        if (channelValue < 0 || channelValue > 255) {
            return false
        }

        if (channelValue == 0) {
            currentValues.remove(channelNo)
        } else {
            currentValues[channelNo] = channelValue.toShort()
        }
        return true
    }

    fun getValue(channelNo: Int): Short {
        return currentValues[channelNo] ?: 0
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


    @ObsoleteCoroutinesApi
    private suspend fun run(coroutineScope: CoroutineScope) {
        var isClosed = false

        sendCurrentValues()

        val ticker = ticker(25)

        var consecutiveErrors = 0

        coroutineScope.launch(newSingleThreadContext("ArtNetThread")) {
            while(coroutineContext.isActive && !isClosed) {
                try {
                    select<Unit> {
                        ticker.onReceiveOrNull {
                            if (it == null) {
                                return@onReceiveOrNull
                            }

                            if (transmissionNeeded.receiveOrNull() == null) {
                                isClosed = true
                                ticker.cancel()
                                return@onReceiveOrNull
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

    private fun runChannelChanger(coroutineScope: CoroutineScope, channelNo: Int, channel: Channel<ChannelUpdatePayload>) {
        var isClosed = false

        var ticker: ReceiveChannel<Unit>? = null
        var currentFadeTargetValue = 0.toShort()
        var targetTickCount = 0
        var tickerStartValue = 0.toShort()
        var tickerLastSetValue = 0.toShort()
        var tickerStartMs = 0L
        var tickerLengthMs = 0L

        var currentTickStepValue = 0.0

        coroutineScope.launch(Dispatchers.Default) {
            while (coroutineScope.isActive && !isClosed) {
                select<Unit> {
                    channel.onReceiveOrNull {
                        if (it == null) {
                            isClosed = true
                            return@onReceiveOrNull
                        }

                        ticker?.cancel()
                        ticker = null

                        val currentValue = currentValues[channelNo] ?: 0

                        val numberOfSteps = if (it.change.fadeMs == 0L) {
                            1
                        } else {
                            max(1, (it.change.fadeMs / 5).toInt())
                        }

                        val valueChange = it.change.newValue - currentValue
                        val stepMs = it.change.fadeMs / numberOfSteps
                        currentTickStepValue = valueChange.toDouble() / numberOfSteps

                        if (numberOfSteps > 1) {
                            targetTickCount = numberOfSteps
                            tickerStartValue = currentValue
                            currentFadeTargetValue = it.change.newValue.toShort()
                            ticker = ticker(stepMs, context = coroutineContext)
                            tickerStartMs = System.currentTimeMillis()
                            tickerLengthMs = it.change.fadeMs

                            val newValue = if (currentTickStepValue > 0) {
                                floor(currentValue + currentTickStepValue).toShort()
                            } else {
                                ceil(currentValue + currentTickStepValue).toShort()
                            }
                            currentValues[channelNo] = newValue

                            tickerLastSetValue = newValue
                        } else {
                            currentValues[channelNo] = it.change.newValue.toShort()
                        }

                        it.updateNotificationChannel.send(Unit)
                    }

                    val currentTicker = ticker
                    if (currentTicker != null) {
                        currentTicker.onReceive {
                            val timeRunningMs = System.currentTimeMillis() - tickerStartMs
                            val currentTickCount = timeRunningMs / 5.0

                            val newValue: Short
                            if (timeRunningMs >= tickerLengthMs) {
                                currentTicker.cancel()
                                ticker = null
                                newValue = currentFadeTargetValue
                            } else {
                                newValue = if (currentTickStepValue > 0) {
                                    floor(tickerStartValue + (currentTickCount * currentTickStepValue)).toShort()
                                } else {
                                    ceil(tickerStartValue + (currentTickCount * currentTickStepValue)).toShort()
                                }
                            }

                            currentValues[channelNo] = newValue

                            if (newValue != tickerLastSetValue) {
                                transmissionNeeded.send(Unit)
                            }
                            tickerLastSetValue = newValue

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
