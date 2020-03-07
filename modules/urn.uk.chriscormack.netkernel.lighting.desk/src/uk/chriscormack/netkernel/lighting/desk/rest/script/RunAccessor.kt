package uk.chriscormack.netkernel.lighting.desk.rest.script

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.script.NetKernelKotlinScriptCompileException
import org.netkernel.mod.hds.IHDSDocument

class RunAccessor: ScriptTestAccessor() {
    override fun SourceRequestContext.onSource() {
        val scriptToRun = source<String>("httpRequest:/body")

        val (scriptResult, error) = try {
            val scriptResult = source<Any?>("active:lightingKotlinScript") {
                argumentByValue("operator", scriptToRun)
            }
            if (scriptResult != null) {
                println(scriptResult::class.java)
            }
            Pair(scriptResult, null)
        } catch (e: Exception) {
            val scriptException = generateSequence(e as Throwable) { it.cause }.last()

            Pair(null, scriptException)
        }

        val resp = response {
            sourceRequest<Any>("active:JSONFromHDS") {
                argumentByValue("operand") {
                    hds {
                        node("runResult") {
                            if (error == null) {
                                node("status", "success")
                                if (scriptResult is IHDSDocument) {
                                    node("result") {
                                        builder.appendChildren(scriptResult.reader)
                                    }
                                } else {
                                    node("result", scriptResult)
                                }
                            } else {
                                if (error is NetKernelKotlinScriptCompileException) {
                                    node("status", "compileError")
                                    node("error") {
                                        builder.appendChildren(reportAsDoc(error.report).reader)
                                    }
                                } else {
                                    node("status", "exception")
                                    node("error") {
                                        builder.appendChildren(error.asDoc().reader)
                                    }
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
