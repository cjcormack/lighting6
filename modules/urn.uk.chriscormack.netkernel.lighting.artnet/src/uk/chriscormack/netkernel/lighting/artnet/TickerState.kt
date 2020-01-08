package uk.chriscormack.netkernel.lighting.artnet

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import kotlin.coroutines.CoroutineContext
import kotlin.math.ceil
import kotlin.math.floor

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal class TickerState(private val controller: ArtNetController, coroutineContext: CoroutineContext, private val channelNo: Int, numberOfSteps: Int, channelUpdatePayload: ArtNetController.ChannelUpdatePayload) {
    val ticker: ReceiveChannel<Unit>

    private val targetValue: Short
    private val startValue: Short
    private val stepValue: Double

    private val startMs: Long
    private val fadeMs: Long

    private var lastSetValue: Short

    init {
        val currentValue = controller.currentValues[channelNo] ?: 0

        val valueChange = channelUpdatePayload.change.newValue - currentValue
        val stepMs = channelUpdatePayload.change.fadeMs / numberOfSteps

        stepValue = valueChange.toDouble() / numberOfSteps

        startValue = currentValue
        lastSetValue = currentValue

        targetValue = channelUpdatePayload.change.newValue.toShort()
        ticker = ticker(stepMs, context = coroutineContext)
        startMs = System.currentTimeMillis()
        fadeMs = channelUpdatePayload.change.fadeMs
    }

    suspend fun setValue(currentTickTimeMs: Long = System.currentTimeMillis() - startMs): Boolean {
        val currentTickCount = if (currentTickTimeMs > 0) {
            currentTickTimeMs / controller.fadeTickMs.toDouble()
        } else {
            1.0
        }

        val hasFinished: Boolean
        val newValue: Short

        if (currentTickTimeMs >= fadeMs) {
            ticker.cancel()
            hasFinished = true
            newValue = targetValue
        } else {
            hasFinished = false
            newValue = if (stepValue > 0) {
                floor(startValue + (currentTickCount * stepValue)).toShort()
            } else {
                ceil(startValue + (currentTickCount * stepValue)).toShort()
            }
        }

        if (newValue != lastSetValue) {
            controller.currentValues[channelNo] = newValue
            lastSetValue = newValue
            controller.transmissionNeeded.send(Unit)
        }
        lastSetValue = newValue

        return hasFinished
    }
}
