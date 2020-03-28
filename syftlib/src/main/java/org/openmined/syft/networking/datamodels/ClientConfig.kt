package org.openmined.syft.networking.datamodels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientConfig(
    //todo populate when defined
    @SerialName("name")
    val modelName: String,
    @SerialName("version")
    val modelVersion: String,
    @SerialName("batch_size")
    val batchSize: Long,
    val lr: Float,
    @SerialName("max_updates")
    val maxUpdates: Int
)
