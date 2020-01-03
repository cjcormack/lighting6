package uk.chriscormack.netkernel.lighting.artnet

import org.netkernel.lang.kotlin.knkf.context.RequestContext
import org.netkernel.lang.kotlin.knkf.context.TransreptorRequestContext
import org.netkernel.lang.kotlin.knkf.context.sourcePrimary
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinTransreptor
import org.netkernel.lang.kotlin.util.firstValue
import org.netkernel.mod.hds.IHDSDocument

class ArtNetTransreptor: KotlinTransreptor<Any, ArtNetController>() {
    init {
        toRepresentation(ArtNetController::class.java)
        declareThreadSafe(false)
    }

    private object Instances {
        val instancesByUniverse = HashMap<Pair<Int, Int>, ArtNetController>()
    }

    override fun TransreptorRequestContext<ArtNetController>.onTransrept() {
        val config = sourcePrimary<IHDSDocument>()

        val universe: Int
        val subnet: Int

        config.reader.let {
            universe = it.firstValue("/config/universe", this, 0)
            subnet = it.firstValue("/config/subnet", this, 0)
        }

        val key = Pair(universe, subnet)

        val controller = Instances.instancesByUniverse[key] ?: {
            val newController = ArtNetController(universe, subnet)
            Instances.instancesByUniverse[key] = newController
            newController
        }()
        response(controller)
    }

    override fun RequestContext.postCommission() {

    }

    override fun RequestContext.preDecommission() {
        Instances.instancesByUniverse.clear()
    }
}
