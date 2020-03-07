package uk.chriscormack.netkernel.lighting.desk.data.scripts

import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.util.SQL
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.mod.hds.HDSFactory
import org.netkernel.mod.hds.IHDSDocument

@Suppress("DuplicatedCode")
class ScriptListAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val resultNode = source<IHDSDocument>("active:sqlPSQuery") {
            argumentByValue("operand", SQL("SELECT id FROM scripts ORDER BY name"))
        }

        source<Unit>("active:attachGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/script/all"))
            argument("id", Identifier("gt:/lighting/data/script/list"))
        }

        val resultBuilder = HDSFactory.newDocument()

        resultBuilder.pushNode("scripts")

        resultNode.reader.getNodes("/resultset/row").forEach {
            val id = it.firstValue<Int>("id", this)

            resultBuilder.pushNode("script")
            resultBuilder.addNode("id", id)
            resultBuilder.addNode("identifier", "res:/lighting/data/script/$id")
            resultBuilder.popNode()
        }

        response(resultBuilder.toDocument(false))
    }
}
