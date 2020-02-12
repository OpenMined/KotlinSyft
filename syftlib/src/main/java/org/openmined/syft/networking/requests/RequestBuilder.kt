package org.openmined.syft.networking.requests

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import org.openmined.syft.SyftJob
import org.openmined.syft.networking.datamodels.NetworkModels

class CommunicationDataFactory {
    companion object DataFactory {
        /**
         * The data curators for http and web socket requests
         */

        //Choosing stable kotlin serialization over default
        private val Json = Json(JsonConfiguration.Stable)

        fun requestCycle(
            workerId: String,
            syftJob: SyftJob,
            ping: String,
            download: String,
            upload: String
        ): JsonObject {
            return json {
                "workerId" to workerId
                "model" to syftJob.modelName
                "ping" to ping
                "download" to download
                "upload" to upload
                if (syftJob.version != null)
                    "version" to syftJob.version
            }
        }

        fun report(workerId: String, requestKey: String, diff: String): JsonObject {
            return json {
                "workerId" to workerId
                "request_key" to requestKey
                "diff" to diff
            }
        }

        fun joinRoom(workerId: String, scopeId: String): JsonObject {
            return json {
                "workerId" to workerId
                "scopeId" to scopeId
            }
        }

        fun internalMessage(
            workerId: String,
            scopeId: String,
            target: String,
            type: WebRTCMessageTypes,
            message: String
        ): JsonObject {
            return json {
                "workerId" to workerId
                "scopeId" to scopeId
                "to" to target
                "type" to type.value
                "data" to message
            }
        }

        fun deserializeSocket(socketMessage: String): NetworkModels {
            return Json.parse(NetworkModels.serializer(), socketMessage)
        }
    }
}

