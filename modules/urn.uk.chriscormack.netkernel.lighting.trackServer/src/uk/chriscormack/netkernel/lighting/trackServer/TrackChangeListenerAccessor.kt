package uk.chriscormack.netkernel.lighting.trackServer

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.layer0.urii.ParsedIdentifierImpl

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class TrackChangeListenerAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val type: String = nkfContext.thisRequest.getArgumentValue(ParsedIdentifierImpl.ARG_ACTIVE_TYPE)

        when (type) {
            "trackServer-addTrackChangeListener" -> {
                onAdd()
            }
            "trackServer-removeTrackChangeListener" -> {
                onRemove()
            }
        }
    }

    private fun SourceRequestContext.onAdd() {
        TrackServerTransport.INSTANCE.registerListener(TrackChangeListener(this))
    }

    private fun SourceRequestContext.onRemove() {
        TrackServerTransport.INSTANCE.unregisterListener(TrackChangeListener(this))
    }
}
