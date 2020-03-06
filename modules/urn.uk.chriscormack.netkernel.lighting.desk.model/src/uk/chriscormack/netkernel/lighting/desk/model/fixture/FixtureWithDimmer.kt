package uk.chriscormack.netkernel.lighting.desk.model.fixture

@ExperimentalUnsignedTypes
@FixtureProperty("Dimmer")
interface FixtureWithDimmer {
    var level: UByte
    fun fadeToLevel(level: UByte, fadeMs: Long)
}
