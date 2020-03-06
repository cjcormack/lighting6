package uk.chriscormack.netkernel.lighting.desk.model

import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor

class ModelAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        source<Unit>("active:lightingKotlinScript") {
            argumentByValue("scriptName", "config")
        }
    }
}
