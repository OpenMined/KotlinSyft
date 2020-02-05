package org.openmined.syft.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import org.openmined.syft.Job

class CommunicationDataFactory {
    companion object DataFactory {
        /**
         * The data curators for http and web socket requests
         */

        //Choosing stable kotlin serialization over default
        private val Json = Json(JsonConfiguration.Stable)

        fun jobRequest(job: Job): JsonObject {

            return json {
                //todo find a better wya to handle workerID
                "workerId" to "" //replaced by signalling client anyway
                "worker" to Json.stringify(Job.serializer(), job)
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

