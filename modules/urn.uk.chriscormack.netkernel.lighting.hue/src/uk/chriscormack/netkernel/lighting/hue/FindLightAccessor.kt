package uk.chriscormack.netkernel.lighting.hue

import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.lang.kotlin.util.values
import org.netkernel.mod.hds.IHDSDocument

class FindLightAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val hueIp = source<String>("arg:hueIp")
        val username = source<String>("arg:username")
        val groupName = source<String>("arg:groupName")
        val lightName = source<String>("arg:lightName")

        val lightList = source<IHDSDocument>("active:hue-lightList") {
            argumentByValue("hueIp", hueIp)
            argumentByValue("username", username)
        }
        val lightMap = lightList.reader.getNodes("/*").map{ it.contextNode.name.toInt() to it }.toMap()


        val groupList = source<IHDSDocument>("active:hue-groupList") {
            argumentByValue("hueIp", hueIp)
            argumentByValue("username", username)
        }

        val groupNode = groupList.reader.getNodes("/*").first { it.firstValue<String>("name") == groupName }

        response {
            groupNode.values<Int>("lights__A/lights", this).first { lightId: Int ->
                val light = lightMap[lightId] ?: throw Exception("No such light with id $lightId")

                light.firstValue<String>("name") == lightName
            }
        }
    }
}
