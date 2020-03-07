package uk.chriscormack.netkernel.lighting.desk.model.fixture.hue

import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureContext
import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureWithColour
import java.awt.Color

@ExperimentalUnsignedTypes
class HueFixtureWithColour(
        val context: FixtureContext,
        val lightId: Int
): FixtureWithColour {
    override val whiteSupport: Boolean = false
    override val amberSupport: Boolean = false
    override val uvSupport: Boolean = false

    override var rgbColor: Color
        get() = throw NotImplementedError()
        set(value) {
            context.setHueLightColor(lightId, value)
        }

    override var whiteLevel: UByte
        get() = 0u
        set(value) {}

    override var amberLevel: UByte
        get() = 0u
        set(value) {}

    override var uvLevel: UByte
        get() = 0u
        set(value) {}

    override fun fadeToColour(rgbColor: Color, fadeMs: Long) {
        context.setHueLightColor(lightId, rgbColor, fadeMs)
    }

    override fun fadeToWhiteLevel(level: UByte, fadeMs: Long) {}

    override fun fadeToAmberLevel(level: UByte, fadeMs: Long) {}

    override fun fadeToUvLevel(level: UByte, fadeMs: Long) {}
}
