package uk.chriscormack.netkernel.lighting.trackServer

import org.netkernel.lang.kotlin.knkf.context.RequestContext
import org.netkernel.layer0.nkf.INKFRequest
import org.netkernel.layer0.util.RequestBuilder
import org.netkernel.layer0.util.XMLUtils
import org.netkernel.mod.hds.IHDSDocument
import org.netkernel.request.impl.RequestScopeLevelImpl
import org.netkernel.urii.IResolution
import org.netkernel.urii.ISpace
import org.w3c.dom.Document


internal class TrackChangeListener(context: RequestContext) {
    private val requestSender: RequestContext.() -> INKFRequest
    private val issuingSpace: ISpace
    private val requestString: String

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
    fun trackChanged(context: RequestContext, trackDetails: IHDSDocument) {
        val req = context.requestSender()

        req.addArgumentByValue("operand", trackDetails)

        context.nkfContext.issueRequest(req)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrackChangeListener

        if (issuingSpace != other.issuingSpace) return false
        if (requestString != other.requestString) return false

        return true
    }

    override fun hashCode(): Int {
        var result = issuingSpace.hashCode()
        result = 31 * result + requestString.hashCode()
        return result
    }
}
