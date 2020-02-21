package uk.chriscormack.netkernel.lighting.desk.script

import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor

class ScriptRunnerAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        responseFromRequest {
            sourceRequest("active:kotlinScript") {
                argument("operator", Identifier("arg:operator"))
                argumentByValue("scriptRuntimeSettings", kotlinScriptSettings)
            }
        }
    }
}
