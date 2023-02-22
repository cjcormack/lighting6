package uk.chriscormack.netkernel.lighting.hue

import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.lang.kotlin.util.values
import org.netkernel.mod.hds.IHDSDocument

class FindGroupAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val hueIp = source<String>("arg:hueIp")
        val username = source<String>("arg:username")
        val groupName = source<String>("arg:groupName")

        val groupList = source<IHDSDocument>("active:hue-groupList") {
            argumentByValue("hueIp", hueIp)
            argumentByValue("username", username)
        }

        response {
            groupList.reader.getNodes("/*").first { it.firstValue<String>("name") == groupName }.contextNode.name.toInt()
        }
    }
}
