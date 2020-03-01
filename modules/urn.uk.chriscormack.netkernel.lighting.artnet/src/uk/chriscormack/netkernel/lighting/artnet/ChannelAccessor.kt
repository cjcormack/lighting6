package uk.chriscormack.netkernel.lighting.artnet

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.netkernel.lang.kotlin.knkf.context.SinkRequestContext
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.context.sourcePrimary

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class ChannelAccessor: BaseArtNetAccessor() {
    override fun SourceRequestContext.onSource() {
        val controller: ArtNetController = sourceConfig()
        val channelNo = source<Int>("arg:channelNo")

        val response = response(controller.getValue(channelNo))
        response.nkfResponse.setNoCache()
    }

    override fun SinkRequestContext.onSink() {
        val controller = sourceConfig()

        val channelNo = source<Int>("arg:channelNo")
        val channelValue = sourcePrimary<Int>()

        val fadeMs = if (exists("arg:fadeMs")) {
            source<Long>("arg:fadeMs")
        } else {
            0
        }

        controller.setValue(channelNo, channelValue, fadeMs)
    }
}
