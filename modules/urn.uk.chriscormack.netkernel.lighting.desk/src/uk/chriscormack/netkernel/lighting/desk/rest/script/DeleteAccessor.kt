package uk.chriscormack.netkernel.lighting.desk.rest.script

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor

class DeleteAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val scriptId = source<Int>("arg:scriptId")

        val success = deleteToEndpoint("lighting6:data:script") {
            argument("id", Identifier(scriptId.toString()))
        }

        val resp = response {
            sourceRequest<Any>("active:JSONFromHDS") {
                argumentByValue("operand") {
                    hds {
                        node("success", success)
                    }
                }
            }.issueForResponse()
        }
        resp.mimeType = "application/json"
    }
}
