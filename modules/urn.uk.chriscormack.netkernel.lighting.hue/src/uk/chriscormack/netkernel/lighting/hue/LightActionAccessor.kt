package uk.chriscormack.netkernel.lighting.hue

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SinkRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor

class LightActionAccessor: KotlinAccessor() {
    override fun SinkRequestContext.onSink() {
        val hueIp = source<String>("arg:hueIp")
        val lightId = source<Int>("arg:lightId")
        val username = source<String>("arg:username")

        val on = source<Boolean>("arg:on")

        source<Any>("active:httpPut") {
            argument("url", Identifier("http://$hueIp/api/$username/lights/$lightId/state"))
            argumentByValue("body", source<Any>("active:JSONFromHDS") {
                argumentByValue("operand") {
                    hds {
                        node("on", on)
                        if (exists("arg:bri")) {
                            node("bri", source<Int>("arg:bri"))
                        }
                        if (exists("arg:hue")) {
                            node("hue", source<Int>("arg:hue"))
                        }
                        if (exists("arg:sat")) {
                            node("sat", source<Int>("arg:sat"))
                        }
                    }
                }
            })
        }
    }
}
