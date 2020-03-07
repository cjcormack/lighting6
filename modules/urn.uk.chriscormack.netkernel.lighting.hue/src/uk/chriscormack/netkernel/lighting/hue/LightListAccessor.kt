package uk.chriscormack.netkernel.lighting.hue

import org.json.JSONObject
import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.mod.hds.IHDSDocument

class LightListAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val hueIp = source<String>("arg:hueIp")
        val username = source<String>("arg:username")

        response {
            sourceRequest<IHDSDocument>("active:JSONToHDS") {
                argumentByRequest("operand") {
                    sourceRequest<JSONObject>("active:httpGet") {
                        argument("url", Identifier("http://$hueIp/api/$username/lights"))
                    }
                }
            }.issueForResponse()
        }
    }
}
