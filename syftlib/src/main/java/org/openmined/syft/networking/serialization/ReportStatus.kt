package org.openmined.syft.networking.serialization

import kotlinx.serialization.Serializable

@Serializable
data class ReportStatus(
    val status: String
) : RequestResponseBody