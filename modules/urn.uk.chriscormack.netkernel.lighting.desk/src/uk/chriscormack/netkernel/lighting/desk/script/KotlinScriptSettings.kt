package uk.chriscormack.netkernel.lighting.desk.script

import org.netkernel.lang.kotlin.knkf.context.RequestContextWithResponse
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.context.TransreptorRequestContext
import org.netkernel.lang.kotlin.script.BaseKotlinScriptTransreptor
import org.netkernel.lang.kotlin.script.BaseScriptRepresentation
import org.netkernel.lang.kotlin.script.NetKernelScriptRuntimeSettings
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*

val kotlinScriptSettings = NetKernelScriptRuntimeSettings(LightingScriptRepresentation::class, LightingScriptConfiguration) {
    ScriptEvaluationConfiguration {
        check(it is SourceRequestContext)

        providedProperties(Pair("context", it))
    }
}

fun getLightingKotlinScriptSettings() = NetKernelScriptRuntimeSettings(LightingScriptRepresentation::class, LightingScriptConfiguration) {
    ScriptEvaluationConfiguration {
        check(it is SourceRequestContext)

        providedProperties(Pair("context", it))
    }
}

object LightingScriptConfiguration : ScriptCompilationConfiguration({
    defaultImports("org.netkernel.layer0.nkf.INKFRequestContext", "org.netkernel.lang.kotlin.knkf.context.*", "org.netkernel.lang.kotlin.knkf.*")
    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
    baseClass(LightKotlinScript::class)
})

@KotlinScript(fileExtension = "light.kts", displayName = "Lighting Kotlin Script", compilationConfiguration = LightingScriptConfiguration::class)
abstract class LightKotlinScript(context: SourceRequestContext): RequestContextWithResponse<Any>(context.nkfContext) {
    fun runScript(scriptName: String) {
        source<Unit>("active:lightingKotlinScript") {
            argumentByValue("scriptName", scriptName)
        }
    }
}

class LightingScriptRepresentation(script: CompiledScript<*>): BaseScriptRepresentation(script)

class LightingScriptTransreptor: BaseKotlinScriptTransreptor<LightingScriptRepresentation>() {
    init {
        this.toRepresentation(LightingScriptRepresentation::class.java)
    }

    override fun TransreptorRequestContext<LightingScriptRepresentation>.onTransrept() {
        response {
            LightingScriptRepresentation(performCompilation(LightingScriptConfiguration))
        }
    }
}
