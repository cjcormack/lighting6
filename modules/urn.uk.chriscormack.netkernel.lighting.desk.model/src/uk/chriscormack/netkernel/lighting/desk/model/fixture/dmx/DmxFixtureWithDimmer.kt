package uk.chriscormack.netkernel.lighting.desk.model.fixture.dmx

import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureContext
import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureWithDimmer

@ExperimentalUnsignedTypes
class DmxFixtureWithDimmer(val context: FixtureContext, val dimmerChannelNo: Int): FixtureWithDimmer {
    override var level: UByte
        get() = context.getDmxValue(dimmerChannelNo)
        set(value) = context.setDmxValue(dimmerChannelNo, value)

    override fun fadeToLevel(level: UByte, fadeMs: Long) {
        context.setDmxValue(dimmerChannelNo, level, fadeMs)
    }
}
