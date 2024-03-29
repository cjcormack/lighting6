package uk.chriscormack.netkernel.lighting.desk.rest.script

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.script.NetKernelKotlinScriptCompilationResult
import org.netkernel.lang.kotlin.script.NetKernelKotlinScriptCompileException
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.mod.hds.IHDSDocument

class CompileAccessor: ScriptTestAccessor() {
    override fun SourceRequestContext.onSource() {
//        val inputDoc = source<IHDSDocument>("active:JSONToHDS") {
//            argument("operand", Identifier("httpRequest:/body"))
//        }
//
//        val scriptToCompile: String
//        val dependencies: List<Identifier>
//
//        inputDoc.reader.let {
//            scriptToCompile = it.firstValue("script")
//            dependencies = emptyList()
//        }
        val scriptToCompile = source<String>("httpRequest:/body")

        val (success, reportDoc) = try {
            val result = source<NetKernelKotlinScriptCompilationResult>("active:lightingKotlinScriptTestCompile") {
                argumentByValue("operator", scriptToCompile)

//                dependencies.forEach {
//                    argument("dependency", it)
//                }
            }
            Pair(true, reportAsDoc(result.report))
        } catch (e: Exception) {
            val scriptException = generateSequence(e as Throwable) { it.cause }.last()

            if (scriptException !is NetKernelKotlinScriptCompileException) {
                throw e
            }

            Pair(false, reportAsDoc(scriptException.report))
        }

        val resp = response {
            sourceRequest<Any>("active:JSONFromHDS") {
                argumentByValue("operand") {
                    hds {
                        node("compileResult") {
                            node("success", success)
                            node("report") {
                                builder.appendChildren(reportDoc.reader)
                            }
                        }
                    }
                }
            }.issueForResponse()
        }
        resp.mimeType = "application/json"
    }
}
