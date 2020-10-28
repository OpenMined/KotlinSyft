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
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * HttpAPI interface is used to implement http API service to PyGrid Server.
 *
 * @See org.openmined.syft.networking.clients.HttpClient
 */
internal interface HttpAPI : CommunicationAPI {

    /**
     * Check connection speed by ping to PyGrid Server.
     *
     * @param isPing Allow PyGrid to differentiate between CheckPing request vs DownloadSpeedTest request.
     * In case of CheckPing it is always true.
     * @param workerId Id of syft worker handling this job.
     * @param random A random integer bit stream.
     */
    @GET("model-centric/speed-test")
    suspend fun checkPing(
        @Query("is_ping") isPing: Int = 1,
        @Query("worker_id") workerId: String,
        @Query("random") random: String
    ): Response<SpeedCheckResponse>

    /**
     * Check download speed from PyGrid Server.
     *
     * @param workerId Id of syft worker handling this job.
     * @param random A random integer bit stream.
     */
    @Streaming
    @GET("model-centric/speed-test")
    suspend fun downloadSpeedTest(
        @Query("worker_id") workerId: String,
        @Query("random") random: String
    ): Response<ResponseBody>

    /**
     * Check upload speed to PyGrid Server by uploading a file using multipart post request.
     *
     * @param workerId Id of syft worker handling this job.
     * @param random A random integer bit stream.
     * @param description Meta-data for upload process.
     * @param file_body A file to be uploaded to check upload speed.
     */
    @Multipart
    @POST("model-centric/speed-test")
    suspend fun uploadSpeedTest(
        @Query("worker_id") workerId: String,
        @Query("random") random: String,
        @Part("description") description: RequestBody,
        @Part file_body: MultipartBody.Part
    ): Response<SpeedCheckResponse>

    /**
     * Download Plans from PyGrid Server.
     *
     * @param workerId Id of syft worker handling this job.
     * @param requestKey A unique key required for authorised communication with PyGrid server.
     * It ensures that only workers accepted for a cycle can receive Plan data from server.
     * @param planId Id of the Plan to be downloaded.
     * @param op_type Format in which Plan operations are defined, Can be torchScript.
     */
    //    @Streaming
    @GET("/model-centric/get-plan")
    suspend fun downloadPlan(
        @Query("worker_id") workerId: String,
        @Query("request_key") requestKey: String,
        @Query("plan_id") planId: String,
        @Query("receive_operations_as") op_type: String
    ): Response<ResponseBody>

    /**
     * Downloads Protocols from PyGrid Server.
     *
     * @param workerId Id of syft worker handling this job.
     * @param requestKey A unique key required for authorised communication with PyGrid server.
     * It ensures that only workers accepted for a cycle can receive Protocol data from server.
     * @param protocolId Id of the Protocol to be downloaded.
     */
    //    @Streaming
    @GET("/model-centric/get-protocol")
    suspend fun downloadProtocol(
        @Query("worker_id") workerId: String,
        @Query("request_key") requestKey: String,
        @Query("protocol_id") protocolId: String
    ): Response<ResponseBody>

    /**
     * Download Model from PyGrid Server.
     *
     * @param workerId Id of syft worker handling this job.
     * @param requestKey A unique key required for authorised communication with PyGrid server.
     * It ensures that only workers accepted for a cycle can receive Model data from server.
     * @param modelId Id of the model to be downloaded.
     */
    //    @Streaming
    @GET("/model-centric/get-model")
    suspend fun downloadModel(
        @Query("worker_id") workerId: String,
        @Query("request_key") requestKey: String,
        @Query("model_id") modelId: String
    ): Response<ResponseBody>

    /**
     * Calls **model-centric/authenticate** for authentication.
     *
     * @param authRequest Contains JWT auth-token. JWT authentication protects the model from sybil attacks.
     */
    @Headers(
        "Content-Type: application/json"
    )
    @POST(AUTH_TYPE)
    override suspend fun authenticate(@Body authRequest: AuthenticationRequest): AuthenticationResponse

    /**
     * Calls **model-centric/cycle-request** for requesting PyGrid server for training cycle.
     * Response of server can be CycleAccept or CycleReject
     * @see CycleResponseData.CycleAccept
     * @see CycleResponseData.CycleReject
     *
     * @param cycleRequest @see org.openmined.syft.networking.datamodels.syft.CycleRequest
     */
    @Headers("Content-Type: application/json")
    @POST(CYCLE_TYPE)
    override suspend fun getCycle(@Body cycleRequest: CycleRequest): CycleResponseData

    /**
     * Calls **model-centric/report** for sending the updated model back to PyGrid.
     *
     * @param reportRequest Contains worker-id and request-key.
     */
    @Headers("Content-Type: application/json")
    @POST(REPORT_TYPE)
    override fun report(@Body reportRequest: ReportRequest): Single<ReportResponse>
}