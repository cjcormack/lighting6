package uk.chriscormack.netkernel.lighting.trackServer

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.layer0.urii.ParsedIdentifierImpl

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class PlayerStateAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val type: String = nkfContext.thisRequest.getArgumentValue(ParsedIdentifierImpl.ARG_ACTIVE_TYPE)

        when (type) {
            "trackServer-play" -> {
                onPlay()
            }
            "trackServer-pause" -> {
                onPause()
            }
        }
    }

    private fun SourceRequestContext.onPlay() {
        TrackServerTransport.INSTANCE.play()
    }

    private fun SourceRequestContext.onPause() {
        TrackServerTransport.INSTANCE.pause()
    }
}
