package uk.chriscormack.netkernel.lighting.desk.script

import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor

class TestCompileAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        response {
            sourceRequest<Boolean>("active:kotlinScriptCompile") {
                argument("operator", Identifier("arg:operator"))
                argumentByValue("scriptRuntimeSettings", kotlinScriptSettings)
            }.issueForResponse()
        }
    }
}
