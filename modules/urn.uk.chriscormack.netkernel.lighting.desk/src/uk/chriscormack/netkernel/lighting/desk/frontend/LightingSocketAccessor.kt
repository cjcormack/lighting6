package uk.chriscormack.netkernel.lighting.desk.frontend

import org.eclipse.jetty.websocket.api.Session
import org.json.JSONObject
import org.netkernel.lang.kotlin.knkf.Identifier
import org.netkernel.lang.kotlin.knkf.LogLevel
import org.netkernel.lang.kotlin.knkf.context.DeleteRequestContext
import org.netkernel.lang.kotlin.knkf.context.NewRequestContext
import org.netkernel.lang.kotlin.knkf.context.SinkRequestContext
import org.netkernel.lang.kotlin.knkf.context.sourcePrimary
import org.netkernel.lang.kotlin.knkf.endpoints.KotlinAccessor
import uk.chriscormack.netkernel.lighting.desk.model.ChannelStateEndpoint
import uk.chriscormack.netkernel.lighting.desk.model.TrackStateEndpoint
import java.io.IOException
import java.util.*

class LightingSocketAccessor : KotlinAccessor() {
    companion object {
        private val connections = HashMap<String, Session>()

        fun channelUpdated(id: Int, level: Int) {
            val channel = JSONObject()
            channel.put("i", id)
            channel.put("l", level)
            val data = JSONObject()
            data.put("c", channel)
            sendMessage("uC", data)
        }

        fun trackStateChanged(isPlaying: Boolean, artist: String, track: String) {
            val data = JSONObject()
            data.put("isPlaying", isPlaying)
            data.put("artist", artist)
            data.put("name", track)
            sendMessage("uT", data)
        }

        private fun sendMessage(type: String, data: JSONObject?) {
            connections.forEach { (_, connection) ->
                if (!connection.isOpen) {
                    return
                }
                val message = JSONObject()
                message.put("type", type)
                if (data != null) {
                    message.put("data", data)
                }
                try {
                    connection.remote.sendString(message.toString())
                } catch (e: IOException) {
                    try {
                        connection.close()
                    } catch (ignored: Exception) {
                    }
                }
            }
        }
    }

    override fun NewRequestContext.onNew() {
        val connection = sourcePrimary<Session>()
        val id = UUID.randomUUID().toString()
        connections[id] = connection

        response(Identifier(id))
    }

    override fun DeleteRequestContext.onDelete() {
        val id = nkfContext.thisRequest.getArgumentValue("socketid")

        val stateManager = connections.remove(id)
        response(stateManager != null)
    }

    override fun SinkRequestContext.onSink() {
        val id = nkfContext.thisRequest.getArgumentValue("socketid")

        val messageJO = sourcePrimary<JSONObject>()

        val connection = connections[id]
        if (connection == null) {
            log(LogLevel.WARNING, "Connection no longer stored, disconnecting")
            sink<Boolean>("wsResponse:/disconnect") {
                primaryArgument(true)
            }
            return
        }

        val type: String = messageJO.getString("type")

        when (type) {
            "ping" -> { // ignore
            }
            "updateChannel" -> {
                val data: JSONObject = messageJO.getJSONObject("data")
                val channel: JSONObject = data.getJSONObject("channel")
                val channelId: Int = channel.getInt("id")
                val channelLevel: Int = channel.getInt("level")

                sink<Int>("active:lighting-setChannel") {
                    primaryArgument(channelLevel)
                    argumentByValue("channelNo", channelId)
                }
            }
            "channelState" -> {
                val channelArray = ArrayList<JSONObject>()
                ChannelStateEndpoint.readCurrentValues {
                    it.forEach { (no, value) ->
                        val channelDetails = JSONObject()
                        channelDetails.put("id", no)
                        channelDetails.put("currentLevel", value)

                        channelArray.add(channelDetails)
                    }
                }

                val channelsData = JSONObject()
                channelsData.put("channels", channelArray)

                val responseJson = JSONObject()
                responseJson.put("type", "channelState")
                responseJson.put("data", channelsData)

                connection.remote.sendString(responseJson.toString())
            }
            "trackDetails" -> {
                val data = JSONObject()

                TrackStateEndpoint.readCurrentValue { playerState, artist, track ->
                    data.put("isPlaying", playerState)
                    data.put("artist", artist)
                    data.put("name", track)
                }

                val responseJson = JSONObject()
                responseJson.put("type", "trackDetails")
                responseJson.put("data", data)

                connection.remote.sendString(responseJson.toString())
            }
            else -> {
                log(LogLevel.INFO, "Unrecognised message type: '$type'")
            }
        }
    }
}
