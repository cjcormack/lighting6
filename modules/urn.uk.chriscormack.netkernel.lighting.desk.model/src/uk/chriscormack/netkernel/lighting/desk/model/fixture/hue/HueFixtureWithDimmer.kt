package uk.chriscormack.netkernel.lighting.desk.model.fixture.hue

import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureContext
import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureWithDimmer

@ExperimentalUnsignedTypes
class HueFixtureWithDimmer(val context: FixtureContext, val lightId: Int): FixtureWithDimmer {
    override var level: UByte
        get() = throw NotImplementedError()
        set(value) = context.setHueLightLevel(lightId, value)

    override fun fadeToLevel(level: UByte, fadeMs: Long) {
        context.setHueLightLevel(lightId, level, fadeMs)
    }
}
