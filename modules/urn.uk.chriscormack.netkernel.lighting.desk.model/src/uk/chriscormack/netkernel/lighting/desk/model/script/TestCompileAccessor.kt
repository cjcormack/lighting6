package uk.chriscormack.netkernel.lighting.desk.model.script

import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.script.NetKernelKotlinScriptCompilationResult

class TestCompileAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        response {
            sourceRequest<NetKernelKotlinScriptCompilationResult>("active:kotlinScriptCompile") {
                argument("operator", Identifier("arg:operator"))
                argumentByValue("scriptRuntimeSettings", kotlinScriptSettings)
            }.issueForResponse()
        }
    }
}
