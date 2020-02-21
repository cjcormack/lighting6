package uk.chriscormack.netkernel.lighting.desk.rest.script

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.script.NetKernelKotlinScriptException

class RunAccessor: ScriptTestAccessor() {
    override fun SourceRequestContext.onSource() {
        val scriptToRun = source<String>("httpRequest:/body")

        val (scriptResult, error) = try {
            val scriptResult = source<Any?>("active:lightingKotlinScript") {
                argumentByValue("operator", scriptToRun)
            }
            Pair(scriptResult, null)
        } catch (e: Exception) {
            val scriptException = generateSequence(e as Throwable) { it.cause }.last()

            if (scriptException !is NetKernelKotlinScriptException) {
                throw e
            }

            Pair(null, scriptException as NetKernelKotlinScriptException)
        }

        val resp = response {
            sourceRequest<Any>("active:JSONFromHDS") {
                argumentByValue("operand") {
                    hds {
                        node("runResult") {
                            node("success", error == null)
                            if (error == null) {
                                node("result", scriptResult)
                            } else {
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
