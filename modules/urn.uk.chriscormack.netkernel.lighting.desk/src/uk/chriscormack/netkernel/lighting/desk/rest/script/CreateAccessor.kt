package uk.chriscormack.netkernel.lighting.desk.rest.script

import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.mod.hds.IHDSDocument

class CreateAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val inputDoc = source<IHDSDocument>("active:JSONToHDS") {
            argument("operand", Identifier("httpRequest:/body"))
        }

        val createdIdentifier = newToEndpoint<IHDSDocument>("lighting6:data:script:create") {
            primaryArgument(inputDoc)
        }

        val resp = response {
            sourceRequest<Any>("active:JSONFromHDS") {
                argumentByRequest("operand") {
                    sourceRequest<IHDSDocument>(createdIdentifier)
                }
            }.issueForResponse()
        }
        resp.mimeType = "application/json"
    }
}
