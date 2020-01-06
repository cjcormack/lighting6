package uk.chriscormack.netkernel.lighting.artnet

import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SinkRequestContext
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.context.sourcePrimary
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.lang.kotlin.util.values
import org.netkernel.mod.hds.HDSFactory
import org.netkernel.mod.hds.IHDSDocument

class BatchChannelAccessor: BaseArtNetAccessor() {
    override fun SourceRequestContext.onSource() {
        val controller: ArtNetController = sourceConfig()

        val resultBuilder = HDSFactory.newDocument()

        val channels = if (exists("arg:channels")) {
            val channelsNode = source<IHDSDocument>("arg:channels")
            channelsNode.reader.values<Int>("//channelNo", this).toList()
        } else {
            1..512
        }

        val attachGTRequest = sourceRequest<Unit>("active:attachGoldenThread") {
            argument("id", Identifier("gt:/lighting/${controller.universe}/${controller.subnet}"))
        }

        channels.forEach { channelNo ->
            resultBuilder.pushNode("channel")
            resultBuilder.addNode("no", channelNo)
            resultBuilder.addNode("value", controller.getValue(channelNo))
            resultBuilder.popNode()

            attachGTRequest.argument("id", Identifier("gt:/lighting/${controller.universe}/${controller.subnet}/$channelNo"))
        }

        attachGTRequest.issue()

        response(resultBuilder.toDocument(false))
    }

    override fun SinkRequestContext.onSink() {
        val controller = sourceConfig()

        val valuesNode = sourcePrimary<IHDSDocument>()

        val valuesToSet = valuesNode.reader.getNodes("//channel").map {
            val channelNo = it.firstValue<Int>("no", this)
            val channelValue = it.firstValue<Int>("value", this)
            val fadeMs = it.firstValue("fadeMs", this, 0)

            channelNo to ChannelChange(channelValue, fadeMs.toLong())
        }
        controller.setValues(valuesToSet)
    }
}
