package uk.chriscormack.netkernel.lighting.desk.model

import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.mod.hds.IHDSDocument

class ModelAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val scriptIdentifier = sourceToEndpoint<Identifier>("lighting6:data:script:byName") {
            argument("name", Identifier("config"))
        }

        val scriptDoc = source<IHDSDocument>(scriptIdentifier)

        source<Unit>("active:lightingKotlinScript") {
            argumentByValue("operator", scriptDoc.reader.getFirstValue("/script/script"))
        }
    }
}
