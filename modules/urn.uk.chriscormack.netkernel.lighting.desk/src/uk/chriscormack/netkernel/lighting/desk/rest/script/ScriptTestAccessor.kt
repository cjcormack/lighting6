package uk.chriscormack.netkernel.lighting.desk.rest.script

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.script.NetKernelKotlinScriptCompileException
import org.netkernel.mod.hds.IHDSDocument

abstract class ScriptTestAccessor: KotlinAccessor() {
    fun NetKernelKotlinScriptCompileException.reportAsDoc(): IHDSDocument {
        return hds {
            node("messages__A") {
                report.forEach { it ->
                    node("messages") {
                        node("severity") {
                            node("name", it.severity.name)
                            node("ordinal", it.severity.ordinal)
                        }
                        node("message", it.message)

                        it.location?.let { location ->
                            node("location", "${location.start.line}:${location.start.col}")
                        }

                        node("sourcePath", it.sourcePath)
                        println("${it.message} ${it.location}")
                    }
                }
            }
        }
    }
    fun Throwable.asDoc(): IHDSDocument {
        return hds {
            node("message", message)
            node("stackTrace__A") {
                this@asDoc.stackTrace.forEach { traceItem ->
                    node("stackTrace", traceItem.toString())
                }
            }
            node("causes__A") {
                generateSequence {
                    cause
                }.forEach {
                    node("causes") {
                        node("message", it.message)
                        node("stackTrace__A") {
                            this@asDoc.stackTrace.forEach { traceItem ->
                                node("stackTrace", traceItem.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}
