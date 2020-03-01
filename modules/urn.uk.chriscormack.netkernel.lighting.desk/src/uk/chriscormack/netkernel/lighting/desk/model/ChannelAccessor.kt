package uk.chriscormack.netkernel.lighting.desk.model

import org.netkernel.lang.kotlin.knkf.context.SinkRequestContext
import org.netkernel.lang.kotlin.knkf.context.sourcePrimary
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor

class ChannelAccessor: KotlinAccessor() {
    override fun SinkRequestContext.onSink() {
        sink<Int>("active:artnet-channel") {
            primaryArgument(sourcePrimary<Int>())
            argumentByValue("channelNo", source<Int>("arg:channelNo"))
            if (exists("arg:fadeMs")) {
                argumentByValue("fadeMs", source<Long>("arg:fadeMs"))
            }
        }
    }
}
