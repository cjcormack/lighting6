package uk.chriscormack.netkernel.lighting.desk.model.fixture.hue

import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureContext
import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureWithDimmer

@ExperimentalUnsignedTypes
class HueFixtureWithDimmer(val context: FixtureContext, val groupId: Int): FixtureWithDimmer {
    override var level: UByte
        get() = throw NotImplementedError()
        set(value) = context.setHueLevel(groupId, value)

    override fun fadeToLevel(level: UByte, fadeMs: Long) {
        context.setHueLevel(groupId, level, fadeMs)
    }
}
