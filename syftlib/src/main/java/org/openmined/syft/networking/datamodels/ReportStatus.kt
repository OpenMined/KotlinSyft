package org.openmined.syft.networking.datamodels

import kotlinx.serialization.Serializable

@Serializable
data class ReportStatus(
    val status: String
) : NetworkModels()