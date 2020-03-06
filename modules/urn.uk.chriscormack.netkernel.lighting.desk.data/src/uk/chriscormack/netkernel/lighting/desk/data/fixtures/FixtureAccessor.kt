package uk.chriscormack.netkernel.lighting.desk.data.fixtures

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.*
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.util.SQL
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.layer0.representation.ByteArrayRepresentation
import org.netkernel.mod.hds.IHDSDocument

class FixtureAccessor: KotlinAccessor() {
    override fun NewRequestContext.onNew() {
        val id = source<IHDSDocument>("active:sqlPSQuery") {
            argumentByValue("operand", SQL("SELECT nextval('fixtures_id_seq') AS next_id"))
        }.reader.firstValue<Long>("//next_id", this).toInt()

        val details = sourcePrimary<IHDSDocument>()

        val scriptId: Int
        val key: String
        val fixtureState: IHDSDocument

        details.reader.getFirstNode("/fixture").let {
            scriptId = it.firstValue("scriptId", this)
            key = it.firstValue("key", this)
            fixtureState = it.getFirstNode("state").toDocument()
        }

        source<Unit>("active:sqlPSUpdate") {
            argumentByValue("operand", SQL("INSERT INTO fixtures (id, script_id, key, fixture_state) VALUES (?, ?, ?, ?)"))
            argumentByValue("param", id)
            argumentByValue("param", scriptId)
            argumentByValue("param", key)
            argumentByValue("param", fixtureState)
        }

        source<Unit>("active:cutGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/fixture/list")) // name may have changed
            argument("id", Identifier("gt:/lighting/data/fixture/$id"))
        }

        response(Identifier("res:/lighting/data/fixture/$id"))
    }

    override fun SourceRequestContext.onSource() {
        val id = source<Int>("arg:id")

        source<Unit>("active:attachGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/fixture/all"))
            argument("id", Identifier("gt:/lighting/data/fixture/$id"))
        }

        val rowNode = source<IHDSDocument>("active:sqlPSQuery") {
            argumentByValue("operand", SQL("SELECT id, script_id, key, fixture_state FROM fixtures WHERE id=?"))
            argumentByValue("param", id)
        }.reader.getFirstNodeOrNull("/resultset/row")
                ?: throw Exception("Fixture $id not found")

        val stateDoc = transrept<IHDSDocument>(ByteArrayRepresentation(rowNode.firstValue<ByteArray>("fixture_state")))

        response {
            hds {
                node("fixture") {
                    node("id", rowNode.firstValue<Int>("id", this@onSource))
                    node("scriptId", rowNode.firstValue<Int>("script_id", this@onSource))
                    node("key", rowNode.firstValue<String>("key", this@onSource))
                    node("state") {
                        builder.appendChildren(stateDoc.reader.getFirstNode("/state"))
                    }
                }
            }
        }
    }

    override fun SinkRequestContext.onSink() {
        val id = source<Int>("arg:id")

        val details = sourcePrimary<IHDSDocument>()

        val fixtureState = details.reader.getFirstNode("/fixture/state")

        source<IHDSDocument>("active:sqlPSUpdate") {
            argumentByValue("operand", SQL("UPDATE fixtures SET fixture_state=? WHERE id=?"))
            argumentByValue("param", fixtureState)
            argumentByValue("param", id)
        }

        source<Unit>("active:cutGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/fixture/$id"))
        }
    }

    override fun DeleteRequestContext.onDelete() {
        val id = source<Int>("arg:id")

        source<IHDSDocument>("active:sqlPSUpdate") {
            argumentByValue("operand", SQL("DELETE FROM fixtures WHERE id=?"))
            argumentByValue("param", id)
        }

        source<Unit>("active:cutGoldenThread") {
            argument("id", Identifier("gt:/lighting/data/fixture/list"))
            argument("id", Identifier("gt:/lighting/data/fixture/$id"))
        }
    }
}
