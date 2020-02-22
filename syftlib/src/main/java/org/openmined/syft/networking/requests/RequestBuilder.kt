package org.openmined.syft.networking.requests

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import org.openmined.syft.Processes.SyftJob
import org.openmined.syft.networking.clients.DATA
import org.openmined.syft.networking.clients.TYPE
import org.openmined.syft.networking.datamodels.SocketResponse


class CommunicationDataFactory {
    companion object DataFactory {
        /**
         * The data curators for http and web socket requests
         */

        //Choosing stable kotlin serialization over default
        private val Json = Json(JsonConfiguration.Stable)

        fun authenticate(): JsonObject {
            return appendType(REQUESTS.AUTHENTICATION)
        }

        @ExperimentalUnsignedTypes
        fun requestCycle(
            workerId: String,
            syftJob: SyftJob,
            ping: String,
            download: String,
            upload: String
        ): JsonObject {
            val data = json {
                "worker_id" to workerId
                "model" to syftJob.modelName
                "ping" to ping
                "download" to download
                "upload" to upload
                if (syftJob.version != null)
                    "version" to syftJob.version
            }
            return appendType(REQUESTS.CYCLE_REQUEST, data)
        }

        fun report(workerId: String, requestKey: String, diff: String): JsonObject {
            val data = json {
                "worker_id" to workerId
                "request_key" to requestKey
                "diff" to diff
            }
            return appendType(REQUESTS.REPORT, data)
        }

        fun joinRoom(workerId: String, scopeId: String): JsonObject {
            val data = json {
                "worker_id" to workerId
                "scope_id" to scopeId
            }
            return appendType(
                WebRTCMessageTypes.WEBRTC_JOIN_ROOM, data
            )
        }

        fun internalMessage(
            workerId: String,
            scopeId: String,
            target: String,
            type: WebRTCMessageTypes,
            message: String
        ): JsonObject {
            val data = json {
                "worker_id" to workerId
                "scope_id" to scopeId
                "to" to target
                "type" to type.value
                "data" to message
            }
            return appendType(REQUESTS.WEBRTC_INTERNAL, data)
        }

        fun deserializeSocket(socketMessage: String): SocketResponse {
            return Json.parse(SocketResponse.serializer(), socketMessage)
        }

        private fun appendType(types: MessageTypes, data: JsonObject? = null) = json {
            TYPE to types.value
            if (data != null)
                DATA to data
        }
    }
}

