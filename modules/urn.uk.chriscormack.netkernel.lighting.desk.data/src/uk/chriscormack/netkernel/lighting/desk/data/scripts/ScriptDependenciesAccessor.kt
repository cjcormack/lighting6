package uk.chriscormack.netkernel.lighting.desk.data.scripts

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.*
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.util.SQL
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.lang.kotlin.util.urlEncode
import org.netkernel.mod.hds.HDSFactory
import org.netkernel.mod.hds.IHDSDocument

@Suppress("DuplicatedCode")
class ScriptDependenciesAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val id = source<Int>("arg:id")

        source<Unit>("active:attachGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/script/all"))
            argument("id", Identifier("gt:/lighting/data/script/$id"))
        }

        val resultBuilder = HDSFactory.newDocument()
        resultBuilder.pushNode("dependencies")

        val resultNode = source<IHDSDocument>("active:sqlPSQuery") {
            argumentByValue("operand", SQL("SELECT other_script_id FROM script_dependencies WHERE script_id=?"))
            argumentByValue("param", id)
        }
        resultNode.reader.getNodes("/resultset/row").forEach {
            val otherScriptId = it.firstValue<Int>("other_script_id", this)
            resultBuilder.pushNode("script")
            resultBuilder.addNode("id", otherScriptId)
            resultBuilder.addNode("identifier", "res:/lighting/data/script/$otherScriptId")
            resultBuilder.popNode()
        }

        response(resultBuilder.toDocument(false))
    }

    override fun SinkRequestContext.onSink() {
        val id = source<Int>("arg:id")

        val details = sourcePrimary<IHDSDocument>()

        source<IHDSDocument>("active:sqlPSUpdate") {
            argumentByValue("operand", SQL("DELETE FROM script_dependencies WHERE script_id=?"))
            argumentByValue("param", id)
        }

        source<IHDSDocument>("active:sqlPSBatch") {
            argumentByValue("operand") {
                hds {
                    node("batch") {
                        node("sql", SQL("INSERT INTO script_dependencies (script_id, other_script_id) VALUES (?, ?)"))
                        node("statements") {
                            details.reader.getNodes("/dependencies/script").forEach {
                                node("statement") {
                                    node("param", id)
                                    node("param", it.firstValue("id"))
                                }
                            }
                        }
                    }
                }
            }
        }

        source<Unit>("active:cutGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/script/$id")) // id will match any positive by-name matches that may no longer be valid
        }
    }

    override fun DeleteRequestContext.onDelete() {
        val id = source<Int>("arg:id")

        source<IHDSDocument>("active:sqlPSUpdate") {
            argumentByValue("operand", SQL("DELETE FROM script_dependencies WHERE script_id=?"))
            argumentByValue("param", id)
        }

        source<Unit>("active:cutGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/script/list"))
            argument("id", Identifier("gt:/lighting/data/script/$id"))
        }
    }
}
