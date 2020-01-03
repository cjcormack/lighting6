package uk.chriscormack.netkernel.lighting.artnet

import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SinkRequestContext
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.context.sourcePrimary

class ChannelAccessor: BaseArtNetAccessor() {
    override fun SourceRequestContext.onSource() {
        val controller: ArtNetController = sourceConfig()
        val channelNo = source<Int>("arg:channelNo")

        source<Unit>("active:attachGoldenThread") {
            argument("id", Identifier("gt:/lighting/${controller.universe}/${controller.subnet}"))
            argument("id", Identifier("gt:/lighting/${controller.universe}/${controller.subnet}/channelNo"))
        }

        response(controller.getValue(channelNo))

    }

    override fun SinkRequestContext.onSink() {
        val controller = sourceConfig()

        val channelNo = source<Int>("arg:channelNo")
        val channelValue = sourcePrimary<Int>()

        controller.setValue(channelNo, channelValue)
    }
}
