package org.openmined.syft.networking.datamodels.syft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openmined.syft.networking.datamodels.NetworkModels

internal const val REPORT_TYPE = "model-centric/report"

@Serializable
internal data class ReportResponse(
    val status: String? = null,
    val error: String? = null
) : NetworkModels()

@Serializable
internal data class ReportRequest(
    @SerialName("worker_id")
    val workerId: String,
    @SerialName("request_key")
    val requestKey: String,
    val diff: String
) : NetworkModels()