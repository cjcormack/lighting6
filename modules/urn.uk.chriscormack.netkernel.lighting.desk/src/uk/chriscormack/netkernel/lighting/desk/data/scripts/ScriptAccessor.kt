package uk.chriscormack.netkernel.lighting.desk.data.scripts

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.*
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.util.SQL
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.lang.kotlin.util.urlEncode
import org.netkernel.mod.hds.IHDSDocument

@Suppress("DuplicatedCode")
class ScriptAccessor: KotlinAccessor() {
    override fun NewRequestContext.onNew() {
        val id = source<IHDSDocument>("active:sqlPSQuery") {
            argumentByValue("operand", SQL("SELECT nextval('scripts_id_seq') AS next_id"))
        }.reader.firstValue<Long>("//next_id", this).toInt()

        val details = sourcePrimary<IHDSDocument>()

        val name: String
        val script: String

        details.reader.getFirstNode("/script").let {
            name = it.firstValue("name", this)
            script = it.firstValue("script", this)
        }

        source<Unit>("active:sqlPSUpdate") {
            argumentByValue("operand", SQL("INSERT INTO scripts (id, name, script) VALUES (?, ?, ?)"))
            argumentByValue("param", id)
            argumentByValue("param", name)
            argumentByValue("param", script)
        }

        source<Unit>("active:cutGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/script/list")) // name may have changed
            argument("id", Identifier("gt:/lighting/data/script/$id"))
        }

        response(Identifier("res:/lighting/data/script/$id"))
    }

    override fun SourceRequestContext.onSource() {
        val id = source<Int>("arg:id")

        source<Unit>("active:attachGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/script/all"))
            argument("id", Identifier("gt:/lighting/data/script/$id"))
        }

        val rowNode = source<IHDSDocument>("active:sqlPSQuery") {
            argumentByValue("operand", SQL("SELECT id, name, script FROM scripts WHERE id=?"))
            argumentByValue("param", id)
        }.reader.getFirstNodeOrNull("/resultset/row")
                ?: throw Exception("Script $id not found")

        response {
            hds {
                node("script") {
                    node("id", rowNode.firstValue<Int>("id", this@onSource))
                    node("name", rowNode.firstValue<String>("name", this@onSource))
                    node("script", rowNode.firstValue<String>("script", this@onSource))
                }
            }
        }
    }

    override fun SinkRequestContext.onSink() {
        val id = source<Int>("arg:id")

        val details = sourcePrimary<IHDSDocument>()

        val name: String
        val script: String

        details.reader.getFirstNode("/script").let {
            name = it.firstValue("name")
            script = it.firstValue("script")
        }

        source<IHDSDocument>("active:sqlPSUpdate") {
            argumentByValue("operand", SQL("UPDATE scripts SET name=?, script=? WHERE id=?"))
            argumentByValue("param", name)
            argumentByValue("param", script)
            argumentByValue("param", id)
        }

        source<Unit>("active:cutGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/script/byName/${name.urlEncode()}")) // name will match any negative by-name matches that were cached
            argument("id", Identifier("gt:/lighting/data/script/$id")) // id will match any positive by-name matches that may no longer be valid
        }
    }

    override fun DeleteRequestContext.onDelete() {
        val id = source<Int>("arg:id")

        source<IHDSDocument>("active:sqlPSUpdate") {
            argumentByValue("operand", SQL("DELETE FROM scripts WHERE id=?"))
            argumentByValue("param", id)
        }

        source<Unit>("active:cutGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/script/list"))
            argument("id", Identifier("gt:/lighting/data/script/$id"))
        }
    }
}
