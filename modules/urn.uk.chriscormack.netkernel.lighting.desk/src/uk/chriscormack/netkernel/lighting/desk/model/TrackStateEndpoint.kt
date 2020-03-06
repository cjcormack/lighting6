package uk.chriscormack.netkernel.lighting.desk.model

import org.netkernel.lang.kotlin.dsl.declarativeRequest.declarativeRequest
import org.netkernel.lang.kotlin.knkf.context.RequestContext
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.mod.hds.IHDSDocument
import uk.chriscormack.netkernel.lighting.desk.frontend.LightingSocketAccessor
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class TrackStateEndpoint: KotlinAccessor() {
    companion object {
        private val valueLock = ReentrantReadWriteLock()
        private var currentTrack: Triple<Boolean, String, String>? = null

        fun readCurrentValue(action: (playerState: Boolean, artist: String, track: String) -> Unit) {
            valueLock.read {
                action(currentTrack?.first ?: false, currentTrack?.second ?: "", currentTrack?.third ?: "")
            }
        }
    }

    init {
        declareThreadSafe(false)
    }

    override fun RequestContext.postCommission() {
        source<Unit>("active:trackServer-addTrackChangeListener") {
            argumentByValue("request") {
                declarativeRequest("active:trackChanged")
            }
        }
    }

    override fun RequestContext.preDecommission() {
        source<Unit>("active:trackServer-removeTrackChangeListener") {
            argumentByValue("request") {
                declarativeRequest("active:trackChanged")
            }
        }
    }

    override fun SourceRequestContext.onSource() {
        val trackDetails = source<IHDSDocument>("arg:operand")

        with(trackDetails.reader.getFirstNode("/track")) {
            val playerState = firstValue<String>("playerState/name") == "PLAYING"
            val artist = firstValue<String>("artist")
            val track = firstValue<String>("title")

            valueLock.write {
                currentTrack = Triple(playerState, artist, track)
            }

            sourceAsync("active:lightingKotlinScript") {
                argumentByValue("scriptName", "track-changed")
            }

            LightingSocketAccessor.trackStateChanged(playerState, artist, track)
        }
    }
}
