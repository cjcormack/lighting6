package uk.chriscormack.netkernel.lighting.desk.model

import java.awt.Color


fun main() {
    println("Hello world!")
}

@ExperimentalUnsignedTypes
internal class HexLight(updater: DmxUpdater, val firstChannel: Int): Light(),
        LightWithDimmer by DmxLightWithDimmerImpl(updater, firstChannel),
        LightWithColour by DmxLightWithColourImpl(updater, firstChannel + 1, firstChannel + 2, firstChannel + 3)

abstract class Light() {
    init {
        println(this::class.supertypes)
    }
}


//fun dmxLight(firstChannel: Int, init: DmxLight.() -> Unit = {}): DmxLight {
//    val light = DmxLight(firstChannel)
//    light.init()
//    return light
//}
//
//class DmxLight(val firstChannel: Int): Light() {
//
//}

internal class DmxUpdater() {

}

interface LightProperty {
}

@ExperimentalUnsignedTypes
interface LightWithDimmer: LightProperty {
    fun setLevel(level: UByte)
}

@ExperimentalUnsignedTypes
internal class DmxLightWithDimmerImpl(val updater: DmxUpdater, val dimmerChannelNo: Int): LightWithDimmer {
    override fun setLevel(level: UByte) {

    }
}

@ExperimentalUnsignedTypes
interface LightWithColour: LightProperty {
    val whiteSupport: Boolean
    val amberSupport: Boolean
    val uvSupport: Boolean

    fun setColor(color: Color, whiteLevel: UByte = 0.toUByte(), amberLevel: UByte = 0.toUByte(), uvLevel: UByte = 0.toUByte())
}

@ExperimentalUnsignedTypes
internal class DmxLightWithColourImpl(
        val updater: DmxUpdater,
        val redChannelNo: Int,
        val greenChannelNo: Int,
        val blueChannelNo: Int,
        val whiteChannelNo: Int? = null,
        val amberChannelNo: Int? = null,
        val uvChannelNo: Int? = null
): LightWithColour {
    override val whiteSupport: Boolean = whiteChannelNo != null
    override val amberSupport: Boolean = amberChannelNo != null
    override val uvSupport: Boolean = uvChannelNo != null

    override fun setColor(color: Color, whiteLevel: UByte, amberLevel: UByte, uvLevel: UByte) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
