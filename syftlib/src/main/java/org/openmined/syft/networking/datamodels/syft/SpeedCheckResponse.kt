package org.openmined.syft.networking.datamodels.syft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openmined.syft.networking.datamodels.NetworkModels

@Serializable
internal data class SpeedCheckResponse(
    @SerialName("error")
    val error: String? = null
) : NetworkModels()