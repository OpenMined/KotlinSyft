package org.openmined.syft.networking.datamodels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val NEW_PEER_TYPE = "peer"
@Serializable
class WebRTCNewPeer(
    @SerialName("worker_id")
    val workerId: String
) : NetworkModels()