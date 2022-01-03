package uk.chriscormack.netkernel.lighting.desk.model.fixture.dmx

import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureContext
import uk.chriscormack.netkernel.lighting.desk.model.fixture.FixtureWithSliders

data class DmxFixtureSliderSettings(
    val channelNo: Int,
    val minValue: UByte = 0u,
    val maxValue: UByte = 255u,
)

@ExperimentalUnsignedTypes
class DmxFixtureWithSliders(val context: FixtureContext, val sliders: Map<String, DmxFixtureSliderSettings>) : FixtureWithSliders {
    override fun setSlider(sliderName: String, level: UByte, fadeMs: Long) {
        val slider = sliders[sliderName] ?: throw Exception("No such slider ('$sliderName')")

        val clippedValue = if (level == UByte.MIN_VALUE) {
            0u
        } else {
            maxOf(slider.minValue, minOf(slider.maxValue, level))
        }

        context.setDmxValue(slider.channelNo, clippedValue, fadeMs)
    }
}
