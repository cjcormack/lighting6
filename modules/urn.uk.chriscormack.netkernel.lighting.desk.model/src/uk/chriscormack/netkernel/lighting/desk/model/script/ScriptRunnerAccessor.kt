package uk.chriscormack.netkernel.lighting.desk.model.script

import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.mod.hds.IHDSDocument

class ScriptRunnerAccessor: KotlinAccessor() {
    override fun SourceRequestContext.onSource() {
        val script = when {
            exists("arg:operator") -> source("arg:operator")
            exists("arg:scriptName") -> {
                val scriptName = source<String>("arg:scriptName")

                val scriptDetailsIdentifier = sourceToEndpoint<Identifier>("lighting6:data:script:byName") {
                    argument("name", Identifier(scriptName))
                }

                val scriptDetails = source<IHDSDocument>(scriptDetailsIdentifier)
                scriptDetails.reader.firstValue<String>("/script/script")
            }
            else -> throw Exception("Neither 'scriptName' or 'operator' arguments supplied")
        }

        responseFromRequest {
            sourceRequest("active:kotlinScript") {
                argumentByValue("operator", script)
                argumentByValue("scriptRuntimeSettings", kotlinScriptSettings)

                this@onSource.nkfContext.thisRequest.let { thisRequest ->
                    repeat(thisRequest.argumentCount) {
                        val argName = thisRequest.getArgumentName(it)
                        // TODO I am pretty sure that there is a better way of doing this!
                        if (argName != "operator" && argName != "scriptName" && argName != "activeType" && argName != "scheme") {
                            argument(argName, Identifier("arg:$argName"))
                        }
                    }
                }
            }
        }
    }
}
