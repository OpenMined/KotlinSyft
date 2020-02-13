package org.openmined.syft.networking.datamodels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val WEBRTC_INTERNAL_TYPE = "webrtc_internal"

@Serializable
data class WebRTCInternalMessage(
    val type: String,
    @SerialName("worker_id")
    val newWorkerId: String,
    @SerialName("sdp_string")
    val sessionDescription: String
) : NetworkModels()