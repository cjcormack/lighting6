package uk.chriscormack.netkernel.lighting.desk

import org.netkernel.lang.kotlin.dsl.declarativeRequest.declarativeRequest
import org.netkernel.lang.kotlin.knkf.context.RequestContext
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import org.netkernel.mod.hds.IHDSDocument

class ChannelChangeEndpoint: KotlinAccessor() {
    override fun RequestContext.postCommission() {
        source<Unit>("active:artnet-addChannelChangeListener") {
            argumentByValue("request") {
                declarativeRequest("active:channelsChanged")
            }
        }
    }

    override fun RequestContext.preDecommission() {
        source<Unit>("active:artnet-removeChannelChangeListener") {
            argumentByValue("request") {
                declarativeRequest("active:channelsChanged")
            }
        }
    }

    override fun SourceRequestContext.onSource() {
        val changedChannelNode = source<IHDSDocument>("arg:operand")
    }
}
