package uk.chriscormack.netkernel.lighting.artnet

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.netkernel.lang.kotlin.knkf.context.RequestContext
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinTransport

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class ArtNetTransport: KotlinTransport() {
    lateinit var controller: ArtNetController

    internal companion object Instances {
        private val instanceByController = HashMap<ArtNetController, ArtNetTransport>()

        fun getInstance(controller: ArtNetController): ArtNetTransport? {
            return instanceByController[controller]
        }
    }

    override fun RequestContext.postCommission() {
        controller = source("param:config")
        instanceByController[controller] = this@ArtNetTransport
    }

    override fun RequestContext.preDecommission() {
        if (!::controller.isInitialized) {
            return
        }

        instanceByController.remove(controller)

        controller.close()
    }
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
val ArtNetController.transport: ArtNetTransport
    get() {
        return checkNotNull(ArtNetTransport.getInstance(this))
    }
