package org.openmined.syft.networking.requests

import org.openmined.syft.networking.serialization.AUTH_TYPE
import org.openmined.syft.networking.serialization.CYCLE_TYPE
import org.openmined.syft.networking.serialization.REPORT_TYPE


interface MessageType {
    val value: String
}

interface CallbackRequestType :
    MessageType {
    override val value: String
}

enum class REQUESTS(
    override val value: String
) : CallbackRequestType {
    AUTHENTICATION(AUTH_TYPE),
    CYCLE(CYCLE_TYPE),
    REPORT(REPORT_TYPE)
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


enum class DOWNLOAD(
    val value: String
) {
    TRAININGPLAN("federated/get-training-plan"),
    PROTOCOL("federated/get-protocol"),
    MODEL("federated/get-model")
}
