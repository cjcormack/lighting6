package uk.chriscormack.netkernel.lighting.desk.rest.script

import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.mod.hds.IHDSDocument

class UpdateAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val scriptId = source<Int>("arg:scriptId")

        val inputDoc = source<IHDSDocument>("active:JSONToHDS") {
            argument("operand", Identifier("httpRequest:/body"))
        }

        sinkToEndpoint<IHDSDocument>("lighting6:data:script") {
            argument("id", Identifier(scriptId.toString()))
            primaryArgument(inputDoc)
        }

        val resp = response {
            sourceRequest<Any>("active:JSONFromHDS") {
                argumentByRequest("operand") {
                    sourceRequestToEndpoint<IHDSDocument>("lighting6:data:script") {
                        argument("id", Identifier(scriptId.toString()))
                    }
                }
            }.issueForResponse()
        }
        resp.mimeType = "application/json"
    }
}
