package uk.chriscormack.netkernel.lighting.desk.model

import org.netkernel.lang.kotlin.knkf.LogLevel
import org.netkernel.lang.kotlin.knkf.context.RequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinTransport
import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureContext
import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureRegister

class ModelAccessor: KotlinTransport() {
    lateinit var fixtureRegister: FixtureRegister

    companion object {
        lateinit var INSTANCE: ModelAccessor
    }

    override fun RequestContext.postCommission() {
        INSTANCE = this@ModelAccessor

        fixtureRegister = FixtureRegister(FixtureContext(transportContext))

        log(LogLevel.WARNING, "Loading fixtures from 'config' script")

        sourceAsync("active:lightingKotlinScript") {
            argumentByValue("scriptName", "config")
        }
    }

    override fun RequestContext.preDecommission() {

    }
}
