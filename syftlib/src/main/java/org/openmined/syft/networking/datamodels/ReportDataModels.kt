package org.openmined.syft.networking.datamodels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val REPORT_TYPE = "federated/report"

@Serializable
data class ReportResponse(
    val status: String
) : NetworkModels()

@Serializable
data class ReportRequest(
    @SerialName("worker_id")
    val workerId: String,
    @SerialName("request_key")
    val requestKey: String,
    val diff: String
) : NetworkModels()