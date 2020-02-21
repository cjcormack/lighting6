package uk.chriscormack.netkernel.lighting.desk.rest.script

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.script.NetKernelKotlinScriptException
import org.netkernel.mod.hds.IHDSDocument

abstract class ScriptTestAccessor: KotlinAccessor() {
    fun NetKernelKotlinScriptException.reportAsDoc(): IHDSDocument {
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
}
