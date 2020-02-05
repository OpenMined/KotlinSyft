package org.openmined.syft.network

interface MessageType {
    val value: String
}

enum class WebRTCMessageTypes(
    override val value: String
) : MessageType {
    CANDIDATE("candidate"),
    OFFER("offer"),
    ANSWER("answer"),

}

enum class REQUEST(
    override val value: String
) : MessageType {
    JOB("job"),
    WEBRTC_JOIN_ROOM("webrtc: join-room"),
    WEBRTC_INTERNAL_MESSAGE("webrtc: internal-message")
}
