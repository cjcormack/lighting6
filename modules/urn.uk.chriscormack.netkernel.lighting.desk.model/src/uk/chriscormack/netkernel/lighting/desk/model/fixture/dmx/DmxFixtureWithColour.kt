package uk.chriscormack.netkernel.lighting.desk.model.fixture.dmx

import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureContext
import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureWithColour
import java.awt.Color

@ExperimentalUnsignedTypes
class DmxFixtureWithColour(
        val context: FixtureContext,
        val redChannelNo: Int,
        val greenChannelNo: Int,
        val blueChannelNo: Int,
        val whiteChannelNo: Int? = null,
        val amberChannelNo: Int? = null,
        val uvChannelNo: Int? = null
): FixtureWithColour {
    override val whiteSupport: Boolean = whiteChannelNo != null
    override val amberSupport: Boolean = amberChannelNo != null
    override val uvSupport: Boolean = uvChannelNo != null

    override var rgbColor: Color
        get() {
            val redLevel = context.getDmxValue(redChannelNo).toInt()
            val greenLevel = context.getDmxValue(greenChannelNo).toInt()
            val blueLevel = context.getDmxValue(blueChannelNo).toInt()

            return Color(redLevel, greenLevel, blueLevel)
        }
        set(value) {
            context.setDmxValue(redChannelNo, value.red.toUByte())
            context.setDmxValue(greenChannelNo, value.green.toUByte())
            context.setDmxValue(blueChannelNo, value.blue.toUByte())
        }

    override var whiteLevel: UByte
        get() = if (whiteChannelNo != null) {
            context.getDmxValue(whiteChannelNo)
        } else {
            0.toUByte()
        }
        set(value) {
            if (whiteChannelNo != null) {
                context.setDmxValue(whiteChannelNo, value)
            }
        }

    override var amberLevel: UByte
        get() = if (amberChannelNo != null) {
            context.getDmxValue(amberChannelNo)
        } else {
            0.toUByte()
        }
        set(value) {
            if (amberChannelNo != null) {
                context.setDmxValue(amberChannelNo, value)
            }
        }

    override var uvLevel: UByte
        get() = if (uvChannelNo != null) {
            context.getDmxValue(uvChannelNo)
        } else {
            0.toUByte()
        }
        set(value) {
            if (uvChannelNo != null) {
                context.setDmxValue(uvChannelNo, value)
            }
        }

    override fun fadeToColour(rgbColor: Color, fadeMs: Long) {
        context.setDmxValue(redChannelNo, rgbColor.red.toUByte(), fadeMs)
        context.setDmxValue(greenChannelNo, rgbColor.green.toUByte(), fadeMs)
        context.setDmxValue(blueChannelNo, rgbColor.blue.toUByte(), fadeMs)
    }

    override fun fadeToWhiteLevel(level: UByte, fadeMs: Long) {
        if (whiteChannelNo != null) {
            context.setDmxValue(whiteChannelNo, level)
        }
    }

    override fun fadeToAmberLevel(level: UByte, fadeMs: Long) {
        if (amberChannelNo != null) {
            context.setDmxValue(amberChannelNo, level)
        }
    }

    override fun fadeToUvLevel(level: UByte, fadeMs: Long) {
        if (uvChannelNo != null) {
            context.setDmxValue(uvChannelNo, level)
        }
    }
}
