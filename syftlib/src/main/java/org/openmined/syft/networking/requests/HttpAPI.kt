package org.openmined.syft.networking.requests

import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.openmined.syft.networking.datamodels.syft.AUTH_TYPE
import org.openmined.syft.networking.datamodels.syft.AuthenticationRequest
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.CYCLE_TYPE
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.REPORT_TYPE
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.networking.datamodels.syft.SpeedCheckResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Streaming

interface HttpAPI : CommunicationAPI {

    @GET("federated/speed-test")
    fun checkPing(
        @Query("is_ping") isPing: Int = 1,
        @Query("worker_id") workerId: String,
        @Query("random") random: String
    ): Single<Response<SpeedCheckResponse>>

    @Streaming
    @GET("federated/speed-test")
    fun downloadSpeedTest(
        @Query("worker_id") workerId: String,
        @Query("random") random: String
    ): Single<Response<ResponseBody>>

    @Multipart
    @POST("federated/speed-test")
    fun uploadSpeedTest(
        @Query("worker_id") workerId: String,
        @Query("random") random: String,
        @Part("description") description: RequestBody,
        @Part file_body: MultipartBody.Part
    ): Single<Response<SpeedCheckResponse>>

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
    override fun authenticate(authRequest: AuthenticationRequest): Single<AuthenticationResponse>

    @POST(CYCLE_TYPE)
    override fun getCycle(@Body cycleRequest: CycleRequest): Single<CycleResponseData>

    @POST(REPORT_TYPE)
    override fun report(@Body reportRequest: ReportRequest): Single<ReportResponse>
}