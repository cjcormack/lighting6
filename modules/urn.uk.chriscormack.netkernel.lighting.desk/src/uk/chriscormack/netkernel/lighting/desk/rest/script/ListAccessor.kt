package uk.chriscormack.netkernel.lighting.desk.rest.script

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.mod.hds.IHDSDocument

class ListAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val scriptList = source<IHDSDocument>("res:/lighting/data/scripts")

        val listDoc = hds {
            node("scripts__A") {
                scriptList.reader.getNodes("/scripts/script").forEach { scriptNode ->
                    node("scripts") {
                        val fullScriptDocument = source<IHDSDocument>(scriptNode.firstValue<String>("identifier"))
                        builder.appendChildren(fullScriptDocument.reader.getFirstNode("/script"))
                    }
                }
            }
        }

        val resp = response {
            sourceRequest<Any>("active:JSONFromHDS") {
                argumentByValue("operand", listDoc)
            }.issueForResponse()
        }
        resp.mimeType = "application/json"
    }
}
