package uk.chriscormack.netkernel.lighting.desk.model.script

import org.netkernel.lang.kotlin.knkf.context.RequestContextWithResponse
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.context.TransreptorRequestContext
import org.netkernel.lang.kotlin.script.BaseKotlinScriptTransreptor
import org.netkernel.lang.kotlin.script.BaseScriptRepresentation
import org.netkernel.lang.kotlin.script.NetKernelScriptRuntimeSettings
import uk.chriscormack.netkernel.lighting.desk.model.ModelAccessor
import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureRegister
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*

val kotlinScriptSettings = NetKernelScriptRuntimeSettings(LightingScriptRepresentation::class, LightingScriptConfiguration) {
    ScriptEvaluationConfiguration {
        check(it is SourceRequestContext)

        providedProperties(Pair("context", it), Pair("fixtureRegister", ModelAccessor.INSTANCE.fixtureRegister))
    }
}

object LightingScriptConfiguration : ScriptCompilationConfiguration({
    defaultImports(
            "org.netkernel.layer0.nkf.INKFRequestContext",
            "org.netkernel.lang.kotlin.knkf.context.*",
            "org.netkernel.lang.kotlin.knkf.*",
            "org.netkernel.lang.kotlin.util.*",
            "uk.chriscormack.netkernel.lighting.desk.model.fixture.*",
            "uk.chriscormack.netkernel.lighting.desk.model.fixture.dmx.*",
            "uk.chriscormack.netkernel.lighting.desk.model.fixture.hue.*",
            "org.netkernel.mod.hds.IHDSDocument",
            "java.awt.Color"
    )
    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
    baseClass(LightKotlinScript::class)
})

@KotlinScript(fileExtension = "light.kts", displayName = "Lighting Kotlin Script", compilationConfiguration = LightingScriptConfiguration::class)
abstract class LightKotlinScript(context: SourceRequestContext, val fixtureRegister: FixtureRegister): RequestContextWithResponse<Any>(context.nkfContext) {
    fun runScript(scriptName: String) {
        source<Unit>("active:lightingKotlinScript") {
            argumentByValue("scriptName", scriptName)
        }
    }

    fun setChannel(channelNo: Int, channelLevel: Int, fadeMs: Long = 0) {
        sink<Int>("active:lighting-setChannel") {
            primaryArgument(channelLevel)
            argumentByValue("channelNo", channelNo)
            argumentByValue("fadeMs", fadeMs)
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
