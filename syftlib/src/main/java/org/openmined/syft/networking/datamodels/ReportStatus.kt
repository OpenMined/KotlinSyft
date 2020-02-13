package org.openmined.syft.networking.datamodels

import kotlinx.serialization.Serializable

const val REPORT_TYPE = "federated/report"

@Serializable
data class ReportStatus(
    val status: String
) : NetworkModels()