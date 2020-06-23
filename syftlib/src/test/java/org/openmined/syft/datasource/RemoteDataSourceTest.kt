package org.openmined.syft.datasource

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.openmined.syft.execution.PLAN_OP_TYPE
import org.openmined.syft.networking.requests.HttpAPI
import retrofit2.Response
import java.io.InputStream


class RemoteDataSourceTest {

    @Mock
    private lateinit var httpApi: HttpAPI

    private lateinit var cut: RemoteDataSource

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        cut = RemoteDataSource(httpApi)
    }

    @Test
    fun `Given a remoteDataSource when downloadModel is invoked then it returns an input stream corresponding to the model`() {
        val workerId = "workerId"
        val requestKey = "requestKey"
        val modelId = "modelId"
        val response = mock<Response<ResponseBody>>()
        val body = mock<ResponseBody>()
        val content = mock<InputStream>()

        whenever(httpApi.downloadModel(workerId, requestKey, modelId)) doReturn Single.just(response)
        whenever(response.body()) doReturn body
        whenever(body.byteStream()) doReturn content

        val result = cut.downloadModel(workerId, requestKey, modelId).test()

        result.assertNoErrors()
                .assertComplete()
                .assertValue(content)

    }

    @Test
    fun `Given a remoteDataSource when downloadPlan is invoked then it returns an input stream corresponding to the plan`() {
        val workerId = "workerId"
        val requestKey = "requestKey"
        val id = "Id"
        val opType = PLAN_OP_TYPE
        val response = mock<Response<ResponseBody>>()
        val body = mock<ResponseBody>()
        val content = mock<InputStream>()

        whenever(httpApi.downloadPlan(workerId, requestKey, id, opType)) doReturn Single.just(response)
        whenever(response.body()) doReturn body
        whenever(body.byteStream()) doReturn content

        val result = cut.downloadPlan(workerId, requestKey, id, opType).test()

        result.assertNoErrors()
                .assertComplete()
                .assertValue(content)

    }

    @Test
    fun `Given a remoteDataSource when downloadProtocol is invoked then it returns an input stream corresponding to the protocol`() {
        val workerId = "workerId"
        val requestKey = "requestKey"
        val id = "Id"
        val response = mock<Response<ResponseBody>>()
        val body = mock<ResponseBody>()
        val content = mock<InputStream>()

        whenever(httpApi.downloadProtocol(workerId, requestKey, id)) doReturn Single.just(response)
        whenever(response.body()) doReturn body
        whenever(body.byteStream()) doReturn content

        val result = cut.downloadProtocol(workerId, requestKey, id).test()

        result.assertNoErrors()
                .assertComplete()
                .assertValue(content)

    }
}