package org.openmined.syft.networking.requests

import io.reactivex.Single
import okhttp3.ResponseBody
import org.openmined.syft.networking.datamodels.syft.AUTH_TYPE
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.CYCLE_TYPE
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.REPORT_TYPE
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface HttpAPI : CommunicationAPI {

//    @Streaming
    @GET("/federated/get-plan")
    fun downloadPlan(
        @Query("worker_id") workerId: String,
        @Query("request_key") requestKey: String,
        @Query("plan_id") planId: String,
        @Query("receive_operations_as") op_type: String
    ): Single<Response<ResponseBody>>

//    @Streaming
    @GET("/federated/get-protocol")
    fun downloadProtocol(
        @Query("worker_id") workerId: String,
        @Query("request_key") requestKey: String,
        @Query("protocol_id") protocolId: String
    ): Single<Response<ResponseBody>>

//    @Streaming
    @GET("/federated/get-model")
    fun downloadModel(
        @Query("worker_id") workerId: String,
        @Query("request_key") requestKey: String,
        @Query("model_id") modelId: String
    ): Single<Response<ResponseBody>>

    @GET(AUTH_TYPE)
    override fun authenticate(): Single<AuthenticationResponse>

    @POST(CYCLE_TYPE)
    override fun getCycle(@Body cycleRequest: CycleRequest): Single<CycleResponseData>

    @POST(REPORT_TYPE)
    override fun report(@Body reportRequest: ReportRequest): Single<ReportResponse>
}