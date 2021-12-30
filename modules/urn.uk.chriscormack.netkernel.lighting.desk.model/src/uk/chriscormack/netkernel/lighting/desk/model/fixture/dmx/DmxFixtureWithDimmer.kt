package uk.chriscormack.netkernel.lighting.desk.model.fixture.dmx

import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureContext
import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureWithDimmer
import kotlin.math.min

@ExperimentalUnsignedTypes
class DmxFixtureWithDimmer(val context: FixtureContext, val dimmerChannelNo: Int, val maxDimmerLevel: UByte = 0u): FixtureWithDimmer {
    override var level: UByte
        get() = context.getDmxValue(dimmerChannelNo)
        set(value) = context.setDmxValue(dimmerChannelNo, min(maxDimmerLevel.toUInt(), value.toUInt()).toUByte())

    override fun fadeToLevel(level: UByte, fadeMs: Long) {
        context.setDmxValue(dimmerChannelNo, min(maxDimmerLevel.toUInt(), level.toUInt()).toUByte(), fadeMs)
    }
}
