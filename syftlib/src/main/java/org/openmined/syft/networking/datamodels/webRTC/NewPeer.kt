package org.openmined.syft.networking.datamodels.webRTC

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openmined.syft.networking.datamodels.NetworkModels

const val NEW_PEER_TYPE = "peer"

@Serializable
internal data class NewPeer(
    @SerialName("worker_id")
    val workerId: String
) : NetworkModels()
