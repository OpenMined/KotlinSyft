package org.openmined.kotlinsyft

object Constants {
    const val WEBRTC_JOIN_ROOM = "webrtc: join-room"
    const val WEBRTC_INTERNAL_MESSAGE = "webrtc: internal-message"
    const val WEBRTC_PEER_LEFT = "webrtc: peer-left"

    object WEBRTC_PEER_CONFIG {
        val iceServer_urls = arrayOf(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
            "stun:stun2.l.google.com:19302",
            "stun:stun3.l.google.com:19302",
            "stun:stun4.l.google.com:19302"
        )
    }

    object WEBRTC_PEER_OPTIONS {
        const val DtlsSrtpKeyAgreement = true
        const val RtpDataChannels = true
    }
}