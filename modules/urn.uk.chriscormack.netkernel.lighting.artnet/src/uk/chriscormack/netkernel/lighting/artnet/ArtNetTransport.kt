package uk.chriscormack.netkernel.lighting.artnet

import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.context.RequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinTransport
import java.util.concurrent.ConcurrentHashMap

class ArtNetTransport: KotlinTransport(), IChannelChangeListener {
    lateinit var controller: ArtNetController

    internal companion object Instances {
        private val instanceByController = HashMap<ArtNetController, ArtNetTransport>()

        fun getInstance(controller: ArtNetController): ArtNetTransport? {
            return instanceByController[controller]
        }
    }

    override fun RequestContext.postCommission() {
        controller = source("param:config")
        controller.registerListener(this@ArtNetTransport)
        instanceByController[controller] = this@ArtNetTransport
    }

    override fun RequestContext.preDecommission() {
        if (!::controller.isInitialized) {
            return
        }

        instanceByController.remove(controller)

        controller.unregisterListener(this@ArtNetTransport)
        controller.close()
    }

    override fun channelsChanged(changes: Map<Int, Int>) {
        val cutRequest = transportContext.sourceRequest<Unit>("active:cutGoldenThread")
        changes.forEach { (channelNo, _) ->
            cutRequest.argument("id", Identifier("gt:/lighting/${controller.universe}/${controller.subnet}/$channelNo"))
        }
        cutRequest.issue()
    }
}

val ArtNetController.transport: ArtNetTransport
    get() {
        return checkNotNull(ArtNetTransport.getInstance(this))
    }
