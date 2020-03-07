package uk.chriscormack.netkernel.lighting.hue

import org.netkernel.lang.kotlin.dsl.hds.hds
import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SinkRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor

class GroupActionAccessor: KotlinAccessor() {
    override fun SinkRequestContext.onSink() {
        val hueIp = source<String>("arg:hueIp")
        val groupId = source<Int>("arg:groupId")
        val username = source<String>("arg:username")

        val on = source<Boolean>("arg:on")

        source<Any>("active:httpPut") {
            argument("url", Identifier("http://$hueIp/api/$username/groups/$groupId/action"))
            argumentByValue("body", source<Any>("active:JSONFromHDS") {
                argumentByValue("operand") {
                    hds {
                        if (exists("arg:on")) {
                            node("on", source<Boolean>("arg:on"))
                        }
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
