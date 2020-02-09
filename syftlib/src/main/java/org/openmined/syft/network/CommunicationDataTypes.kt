package org.openmined.syft.network

import kotlinx.serialization.json.JsonObject

interface MessageType {
    val value: String
}

interface CallbackRequestType : MessageType {
    override val value: String
    fun handleResponse(response: JsonObject)
}

enum class WebRTCMessageTypes(
    override val value: String
) : MessageType {
    CANDIDATE("candidate"),
    OFFER("offer"),
    ANSWER("answer"),
    WEBRTC_JOIN_ROOM("webrtc: join-room"),
    WEBRTC_INTERNAL_MESSAGE("webrtc: internal-message")

}

enum class REQUEST(
    override val value: String
) : CallbackRequestType {
    AUTHENTICATION("federated/authenticate") {
        override fun handleResponse(response: JsonObject) {
            response["workerId"]
        }
    },
    CYCLE("federated/cycle-request") {
        override fun handleResponse(response: JsonObject) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    },
    TRAININGPLAN("federated/get-training-plan") {
        override fun handleResponse(response: JsonObject) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    },
    PROTOCOL("federated/get-protocol") {
        override fun handleResponse(response: JsonObject) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    },
    MODEL("federated/get-model") {
        override fun handleResponse(response: JsonObject) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}

enum class POST(
    override val value: String
) : CallbackRequestType {
    REPORT("federated/report") {
        override fun handleResponse(response: JsonObject) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}
