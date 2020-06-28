package org.openmined.syft.integration.clients

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import io.reactivex.Single
import okhttp3.ResponseBody.Companion.toResponseBody
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.datamodels.syft.SpeedCheckResponse
import org.openmined.syft.networking.requests.HttpAPI
import retrofit2.Response
import java.io.InputStream

private const val MB = 1024 * 1024

class HttpClientMock(
    private val pingSuccess: Boolean,
    private val downloadSpeedSuccess: Boolean,
    private val uploadSuccess: Boolean,
    private val downloadPlanSuccess: Boolean,
    private val downloadModelSuccess: Boolean
) {

    private val mockedClient = mock<HttpAPI>()
    private val modelFile: InputStream =
            javaClass.classLoader?.getResourceAsStream("proto_files/model_params.pb")!!
    private val planFile: InputStream =
            javaClass.classLoader?.getResourceAsStream("proto_files/plan.pb")!!

    init {
        mockedClient.stub {
            on { checkPing(any(), any(), any()) }.thenReturn(
                if (pingSuccess) Single.just(Response.success(200, SpeedCheckResponse()))
                else Single.just(
                    Response.error(
                        403,
                        SpeedCheckResponse("error message").toString().toResponseBody()
                    )
                )
            )

            on { uploadSpeedTest(any(), any(), any(), check {}) }.thenReturn(
                if (uploadSuccess)
                    Single.just(Response.success(SpeedCheckResponse()))
                else
                    Single.just(
                        Response.error(
                            403,
                            SpeedCheckResponse("error message").toString().toResponseBody()
                        )
                    )
            )

            on { downloadSpeedTest(any(), any()) }.thenReturn(
                if (downloadSpeedSuccess)
                    Single.just(Response.success(ByteArray(64 * MB).toResponseBody()))
                else
                    Single.just(
                        Response.error(
                            403,
                            SpeedCheckResponse("error message").toString().toResponseBody()
                        )
                    )
            )

            on { downloadPlan(any(), any(), eq("1"), eq("torchscript")) }.thenReturn(
                if (downloadPlanSuccess)
                    Single.just(Response.success(planFile.readBytes().toResponseBody()))
                else
                    Single.just(
                        Response.error(
                            403,
                            SpeedCheckResponse("error message").toString().toResponseBody()
                        )
                    )
            )

            on { downloadModel(any(), any(), eq("2")) }.thenReturn(
                if (downloadModelSuccess)
                    Single.just(Response.success(modelFile.readBytes().toResponseBody()))
                else
                    Single.just(
                        Response.error(
                            403,
                            SpeedCheckResponse("error message").toString().toResponseBody()
                        )
                    )
            )
        }
    }

    fun getMockedClient() = HttpClient(mockedClient)
}