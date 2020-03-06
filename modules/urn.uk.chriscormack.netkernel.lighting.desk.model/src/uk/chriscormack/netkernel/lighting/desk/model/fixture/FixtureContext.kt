package uk.chriscormack.netkernel.lighting.desk.model.fixture

import org.netkernel.lang.kotlin.knkf.context.RequestContext
import uk.chriscormack.netkernel.lighting.desk.model.ChannelStateEndpoint

@ExperimentalUnsignedTypes
class FixtureContext(private val context: RequestContext) {
    fun getDmxValue(channelNo: Int): UByte {
        return ChannelStateEndpoint.INSTANCE.getCurrentValue(channelNo).toUByte()
    }

    fun setDmxValue(channelNo: Int, channelLevel: UByte, fadeMs: Long = 0) {
        context.sink<Int>("active:lighting-setChannel") {
            primaryArgument(channelLevel.toInt())
            argumentByValue("channelNo", channelNo)
            argumentByValue("fadeMs", fadeMs)
        }
    }
}
