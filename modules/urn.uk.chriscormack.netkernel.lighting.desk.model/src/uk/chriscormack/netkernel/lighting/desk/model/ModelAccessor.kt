package uk.chriscormack.netkernel.lighting.desk.model

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import org.netkernel.lang.kotlin.knkf.LogLevel
import org.netkernel.lang.kotlin.knkf.context.RequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinTransport
import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureContext
import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureRegister

class ModelAccessor: KotlinTransport() {
    lateinit var fixtureRegister: FixtureRegister

    companion object {
        lateinit var INSTANCE: ModelAccessor

        var isClosed = false

        internal fun CoroutineScope.run(context: RequestContext) {
            val ticker = ticker(500)

            var consecutiveErrors = 0

            var step = 0

            launch(newSingleThreadContext("LightingRunLoop")) {
                while(coroutineContext.isActive && !isClosed) {
                    try {
                        select<Unit> {
                            ticker.onReceiveCatching {
                                if (it.isClosed) {
                                    return@onReceiveCatching
                                }

                                context.source<Any>("active:lightingKotlinScript") {
                                    argumentByValue("scriptName", "runloop")
                                    argumentByValue("step", step)
                                }
                                step++
                            }
                        }
                        consecutiveErrors = 0
                    } catch (e: Exception) {
                        if (consecutiveErrors == 0) {
                            e.printStackTrace()
                        }
                        consecutiveErrors++

                        if (consecutiveErrors > 20) {
                            // if too many errors, we'll bail out and let this thing stop. A restart of NK is needed.
                            println("Too many consecutive errors")
                            throw e
                        }
                        delay(10_000)
                    }
                }
            }
        }
    }

    override fun RequestContext.postCommission() {
        INSTANCE = this@ModelAccessor

        fixtureRegister = FixtureRegister(FixtureContext(transportContext))

        log(LogLevel.WARNING, "Loading fixtures from 'config' script")

        source<Any>("active:lightingKotlinScript") {
            argumentByValue("scriptName", "config")
        }

        GlobalScope.launch {
            run(transportContext)
        }
    }

    override fun RequestContext.preDecommission() {
        isClosed = true
    }
}
