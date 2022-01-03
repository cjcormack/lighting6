package uk.chriscormack.netkernel.lighting.desk.model.fixture

@FixtureProperty("Sliders")
interface FixtureWithSliders {
    fun setSlider(sliderName: String, level: UByte, fadeMs: Long = 0)
}
