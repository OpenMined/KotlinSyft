package org.openmined.syft.networking.requests

import io.reactivex.Single
import org.openmined.syft.networking.datamodels.syft.AuthenticationRequest
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse


internal interface CommunicationAPI {
    fun authenticate(authRequest: AuthenticationRequest): Single<AuthenticationResponse>

    fun getCycle(cycleRequest: CycleRequest): Single<CycleResponseData>

    fun report(reportRequest: ReportRequest): Single<ReportResponse>
}