package org.openmined.syft.networking.requests

import org.openmined.syft.networking.datamodels.syft.AuthenticationRequest
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse

internal interface CommunicationAPI {
    suspend fun authenticate(authRequest: AuthenticationRequest): AuthenticationResponse

    suspend fun getCycle(cycleRequest: CycleRequest): CycleResponseData

    suspend fun report(reportRequest: ReportRequest): ReportResponse
}