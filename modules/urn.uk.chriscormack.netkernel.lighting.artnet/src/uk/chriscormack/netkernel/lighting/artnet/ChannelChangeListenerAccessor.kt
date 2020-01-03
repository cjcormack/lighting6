package uk.chriscormack.netkernel.lighting.artnet

import org.netkernel.lang.kotlin.knkf.context.RequestContext
import org.netkernel.lang.kotlin.knkf.context.SourceRequestContext
import org.netkernel.layer0.nkf.*
import org.netkernel.layer0.urii.ParsedIdentifierImpl
import org.netkernel.layer0.util.RequestBuilder
import org.netkernel.layer0.util.XMLUtils
import org.netkernel.mod.hds.HDSFactory
import org.netkernel.request.impl.RequestScopeLevelImpl
import org.netkernel.urii.IResolution
import org.netkernel.urii.ISpace
import org.netkernel.util.Utils
import org.w3c.dom.Document

class ChannelChangeListenerAccessor: BaseArtNetAccessor() {
    override fun SourceRequestContext.onSource() {
        val type: String = nkfContext.thisRequest.getArgumentValue(ParsedIdentifierImpl.ARG_ACTIVE_TYPE)

        when (type) {
            "artnet-addChannelChangeListener" -> {
                onAdd()
            }
            "artnet-removeChannelChangeListener" -> {
                onRemove()
            }
        }
    }

    private fun SourceRequestContext.onAdd() {
        val controller = sourceConfig()
        controller.registerListener(ChannelChangeListener(controller, this))
    }

    private fun SourceRequestContext.onRemove() {
        val controller = sourceConfig()
        controller.unregisterListener(ChannelChangeListener(controller, this))
    }

    class ChannelChangeListener(val controller: ArtNetController, context: RequestContext): IChannelChangeListener {
        private val requestSender: RequestContext.() -> INKFRequest
        private val issuingSpace: ISpace
        val requestString: String

        init {
            val requestArg: Any = context.source("arg:request")

            if (requestArg is INKFRequest) {
                val resolution: IResolution = context.nkfContext.kernelContext.resolveRequest(requestArg)
                issuingSpace = resolution.scope.space
                requestString = requestArg.identifier

                requestSender = {
                    requestArg
                }
            } else {
                val declRequestNode = context.transrept<Document>(requestArg)

                val builder = RequestBuilder(declRequestNode, context.nkfContext.kernelContext.kernel.logger)

                val args = RequestBuilder.Arguments()
                args.addArgumentByValue("id", "dummy")
                var req = builder.buildRequest(context.nkfContext, args, null)
                val resolution: IResolution = context.nkfContext.kernelContext.resolveRequest(req)
                issuingSpace = resolution.scope.space
                requestString = XMLUtils.toXML(declRequestNode, false, true)

                requestSender = {
                    req = builder.buildRequest(this.nkfContext, args, null)
                    val scope = RequestScopeLevelImpl.createOrphanedRootScopeLevel(issuingSpace, this.nkfContext.kernelContext.requestScope)
                    req.setRequestScope(scope)
                    req
                }
            }
        }

        override fun channelsChanged(changes: Map<Int, Int>) {
            val context = controller.transport.transportContext
            val req = context.requestSender()

            val builder = HDSFactory.newDocument()
            builder.pushNode("channels")
            changes.forEach { (no, value) ->
                builder.pushNode("channel")
                builder.addNode("no", no)
                builder.addNode("value", value)
                builder.popNode()
            }

            req.addArgumentByValue("operand", builder.toDocument(false))

            context.nkfContext.issueRequest(req)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ChannelChangeListener

            if (controller != other.controller) return false
            if (issuingSpace != other.issuingSpace) return false
            if (requestString != other.requestString) return false

            return true
        }

        override fun hashCode(): Int {
            var result = controller.hashCode()
            result = 31 * result + issuingSpace.hashCode()
            result = 31 * result + requestString.hashCode()
            return result
        }

//        override fun hashCode(): Int {
//            return requestString.hashCode() xor issuingSpace.hashCode()
//        }


    }
}
