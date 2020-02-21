package uk.chriscormack.netkernel.lighting.desk.data.scripts

import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.ExistsRequestContext
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.util.SQL
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.lang.kotlin.util.urlEncode
import org.netkernel.mod.hds.IHDSDocument

@Suppress("DuplicatedCode")
class ScriptByNameAccessor: KotlinAccessor() {
    override fun ExistsRequestContext.onExists() {
        val name = source<String>("arg:name")

        source<Unit>("active:attachGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/script/all"))
            argument("id", Identifier("gt:/lighting/data/script/list"))
            argument("id", Identifier("gt:/lighting/data/script/byName/${name.urlEncode()}"))
        }

        response {
            source("active:sqlPSBooleanQuery") {
                argumentByValue("operand", SQL("SELECT id FROM scripts WHERE name=?"))
                argumentByValue("param", name)
            }
        }
    }

    override fun SourceRequestContext.onSource() {
        val name = source<String>("arg:name")

        source<Unit>("active:attachGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/script/all"))
            argument("id", Identifier("gt:/lighting/data/script/list"))
            argument("id", Identifier("gt:/lighting/data/script/byName/${name.urlEncode()}"))
        }

        val rowNode = source<IHDSDocument>("active:sqlPSQuery") {
            argumentByValue("operand", SQL("SELECT id FROM scripts WHERE name=?"))
            argumentByValue("param", name)
        }.reader.getFirstNodeOrNull("/resultset/row")
                ?: throw Exception("No script named '$name'")

        val id = rowNode.firstValue<Int>("id", this@onSource)

        source<Unit>("active:attachGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/script/$id"))
        }

        response(Identifier("res:/lighting/data/script/$id"))
    }
}
