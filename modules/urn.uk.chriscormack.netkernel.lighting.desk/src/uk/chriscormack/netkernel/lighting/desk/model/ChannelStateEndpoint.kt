package uk.chriscormack.netkernel.lighting.desk.model

import org.netkernel.lang.kotlin.dsl.declarativeRequest.declarativeRequest
import org.netkernel.lang.kotlin.knkf.context.RequestContext
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.mod.hds.IHDSDocument
import uk.chriscormack.netkernel.lighting.desk.frontend.LightingSocketAccessor
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class ChannelStateEndpoint: KotlinAccessor() {
    companion object {
        private val valuesLock = ReentrantReadWriteLock()
        private val lightingValues = HashMap<Int, Int>()

        fun readCurrentValues(action: (Map<Int, Int>) -> Unit) {
            valuesLock.read {
                action(lightingValues)
            }
        }
    }

    init {
        declareThreadSafe(false)
    }

    override fun RequestContext.postCommission() {
        source<Unit>("active:artnet-addChannelChangeListener") {
            argumentByValue("request") {
                declarativeRequest("active:channelsChanged")
            }
        }

        val channelNodes = source<IHDSDocument>("active:artnet-batchChannel")

        valuesLock.write {
            channelNodes.reader.getNodes("//channel").forEach {
                lightingValues[it.firstValue("no", this)] = it.firstValue("value", this)
            }
        }
    }

    override fun RequestContext.preDecommission() {
        source<Unit>("active:artnet-removeChannelChangeListener") {
            argumentByValue("request") {
                declarativeRequest("active:channelsChanged")
            }
        }

        valuesLock.write {
            lightingValues.clear()
        }
    }

    override fun SourceRequestContext.onSource() {
        val changedChannelNodes = source<IHDSDocument>("arg:operand")

        valuesLock.write {
            changedChannelNodes.reader.getNodes("//channel").forEach {
                val no = it.firstValue<Int>("no", this)
                val value = it.firstValue<Int>("value", this)

                lightingValues[no] = value
                // TODO replace with a proper listener interface
                LightingSocketAccessor.channelUpdated(no, value)
            }
        }
    }
}
