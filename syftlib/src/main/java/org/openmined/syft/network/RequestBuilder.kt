package org.openmined.syft.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.json
import org.openmined.syft.SyftJob

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
            val map = mutableMapOf<String,JsonElement>(
                "workerId" to JsonPrimitive(workerId),
                "model" to JsonPrimitive(syftJob.modelName),
                "ping" to JsonPrimitive(ping),
                "download" to JsonPrimitive(download),
                "upload" to JsonPrimitive(upload)
            )

            if (syftJob.version != null)
                map["version"] = JsonPrimitive(syftJob.version)
            return JsonObject(map)
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

    }
}

