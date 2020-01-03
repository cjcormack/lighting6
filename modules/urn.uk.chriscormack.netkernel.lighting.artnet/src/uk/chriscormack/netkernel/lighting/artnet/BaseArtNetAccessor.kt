package uk.chriscormack.netkernel.lighting.artnet

import org.netkernel.lang.kotlin.knkf.context.RequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor

abstract class BaseArtNetAccessor: KotlinAccessor() {
    internal fun RequestContext.sourceConfig(): ArtNetController {
        return if (exists("arg:config")) {
            source("arg:config")
        } else {
            source("res:/etc/ArtNetConfig.xml")
        }
    }
}
