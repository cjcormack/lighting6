package uk.chriscormack.netkernel.lighting.artnet

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.netkernel.lang.kotlin.knkf.context.SinkRequestContext
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.context.sourcePrimary
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.lang.kotlin.util.values
import org.netkernel.mod.hds.HDSFactory
import org.netkernel.mod.hds.IHDSDocument

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
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

        channels.forEach { channelNo ->
            resultBuilder.pushNode("channel")
            resultBuilder.addNode("no", channelNo)
            resultBuilder.addNode("value", controller.getValue(channelNo))
            resultBuilder.popNode()
        }

        val response = response(resultBuilder.toDocument(false))
        response.nkfResponse.setNoCache()
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
