package org.openmined.syft.networking.requests

import io.reactivex.Single
import org.openmined.syft.networking.datamodels.syft.AuthenticationSuccess
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse


interface CommunicationAPI {
    fun authenticate(): Single<AuthenticationSuccess>

    fun getCycle(cycleRequest: CycleRequest): Single<CycleResponseData>

    fun report(reportRequest: ReportRequest): Single<ReportResponse>
}