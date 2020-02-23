package org.openmined.syft.networking.requests

import io.reactivex.Single
import org.openmined.syft.networking.datamodels.AuthenticationSuccess
import org.openmined.syft.networking.datamodels.CycleRequest
import org.openmined.syft.networking.datamodels.CycleResponseData
import org.openmined.syft.networking.datamodels.ReportRequest
import org.openmined.syft.networking.datamodels.ReportResponse


interface CommunicationAPI {
    fun authenticate(): Single<AuthenticationSuccess>

    fun getCycle(cycleRequest: CycleRequest): Single<CycleResponseData>

    fun report(reportRequest: ReportRequest): Single<ReportResponse>
}