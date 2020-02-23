package org.openmined.syft.networking.requests

import io.reactivex.Single
import okhttp3.ResponseBody
import org.openmined.syft.networking.datamodels.AUTH_TYPE
import org.openmined.syft.networking.datamodels.AuthenticationSuccess
import org.openmined.syft.networking.datamodels.CYCLE_TYPE
import org.openmined.syft.networking.datamodels.CycleRequest
import org.openmined.syft.networking.datamodels.CycleResponseData
import org.openmined.syft.networking.datamodels.REPORT_TYPE
import org.openmined.syft.networking.datamodels.ReportRequest
import org.openmined.syft.networking.datamodels.ReportResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HttpAPI : CommunicationAPI{

    @GET("/federated/get-plan")
    fun downloadPlan(
        @Query("worker_id") workerId: String,
        @Query("request_key") requestKey: String,
        @Query("plan_id") planId: String,
        @Query("receive_operations_as") op_type: String
    ): Single<ResponseBody>

    @GET("/federated/get-protocol")
    fun downloadProtocol(
        @Query("worker_id") workerId: String,
        @Query("request_key") requestKey: String,
        @Query("protocol_id") protocolId: String
    ): Single<ResponseBody>

    @GET("/federated/get-model")
    fun downloadModel(
        @Query("worker_id") workerId: String,
        @Query("request_key") requestKey: String,
        @Query("model_id") modelId: String
    ): Single<ResponseBody>

    @GET(AUTH_TYPE)
    override fun authenticate(): Single<AuthenticationSuccess>

    @POST(CYCLE_TYPE)
    override fun getCycle(@Body cycleRequest: CycleRequest): Single<CycleResponseData>

    @POST(REPORT_TYPE)
    override fun report(@Body reportRequest: ReportRequest): Single<ReportResponse>
}