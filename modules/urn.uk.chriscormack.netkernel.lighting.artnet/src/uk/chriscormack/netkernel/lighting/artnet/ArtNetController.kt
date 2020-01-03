package uk.chriscormack.netkernel.lighting.artnet

import ch.bildspur.artnet.ArtNetClient
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class ArtNetController(val universe: Int, val subnet: Int) {
    private val artnet = ArtNetClient()

    private val transmissionNeeded = Channel<Unit>(Channel.Factory.CONFLATED)

    private val currentValues = ConcurrentHashMap<Int, Short>()

    private var previousSentDmxData = ByteArray(512)

    private val listeners = ArrayList<IChannelChangeListener>()

    init {
        artnet.start()

        @Suppress("EXPERIMENTAL_API_USAGE")
        GlobalScope.launch(newSingleThreadContext("ArtNetThread")) {
            run(coroutineContext)
        }
    }

    fun setValues(valuesToSet: List<Pair<Int, Int>>) {
        var valuesChanged = false

        valuesToSet.forEach {
            if (doSetValue(it.first, it.second)) {
                valuesChanged = true
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
    private suspend fun run(coroutineContext: CoroutineContext) {
        var isClosed = false

        sendCurrentValues()

        val ticker = ticker(25)

        var consecutiveErrors = 0

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
