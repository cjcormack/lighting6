package uk.chriscormack.netkernel.lighting.desk.rest.script

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.script.NetKernelKotlinScriptCompileException

class CompileAccessor: ScriptTestAccessor() {
    override fun SourceRequestContext.onSource() {
        val scriptToCompile = source<String>("httpRequest:/body")

        val error: NetKernelKotlinScriptCompileException? = try {
            source<Unit>("active:lightingKotlinScriptTestCompile") {
                argumentByValue("operator", scriptToCompile)
            }
            null
        } catch (e: Exception) {
            val scriptException = generateSequence(e as Throwable) { it.cause }.last()

            if (scriptException !is NetKernelKotlinScriptCompileException) {
                throw e
            }

            scriptException
        }

        val resp = response {
            sourceRequest<Any>("active:JSONFromHDS") {
                argumentByValue("operand") {
                    hds {
                        node("compileResult") {
                            node("success", error == null)
                            if (error != null) {
                                node("error") {
                                    builder.appendChildren(error.reportAsDoc().reader)
                                }
                            }
                        }
                    }
                }
            }.issueForResponse()
        }
        resp.mimeType = "application/json"
    }
}
