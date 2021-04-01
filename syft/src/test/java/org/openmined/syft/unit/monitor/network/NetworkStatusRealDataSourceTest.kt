package org.openmined.syft.unit.monitor.network

import android.accounts.NetworkErrorException
import android.content.Context
import android.net.ConnectivityManager
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.openmined.syft.common.AbstractSyftWorkerTest
import org.openmined.syft.monitor.network.NetworkStatusModel
import org.openmined.syft.monitor.network.NetworkStatusRealTimeDataSource
import org.openmined.syft.unit.monitor.network.client.DelayedHttpClientMock

@ExperimentalUnsignedTypes
class NetworkStatusRealDataSourceTest : AbstractSyftWorkerTest() {

//    private val workerId = "test id"
//    private val networkStatusModel = NetworkStatusModel()
//
//    private lateinit var networkDataSourceSuccess: NetworkStatusRealTimeDataSource
//    private lateinit var networkDataSourceFailure: NetworkStatusRealTimeDataSource
//
//    @Rule
//    @JvmField
//    var tempFolder = TemporaryFolder()
//
//    @Before
//    fun setUp() {
//        val file = tempFolder.newFile("uploadFile")
//        // Configure client to return success response
//        val httpClientMockSuccess =
//                DelayedHttpClientMock(
//                    pingSuccess = true, downloadSpeedSuccess = true,
//                    uploadSuccess = true, downloadPlanSuccess = true, downloadModelSuccess = true
//                )
//
//        networkDataSourceSuccess = NetworkStatusRealTimeDataSource(
//            httpClientMockSuccess.getMockedClient().apiClient,
//            file.parentFile!!,
//            mock(),
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        )
//
//        // Configure client to return failure response
//        val httpClientMockFailure =
//                DelayedHttpClientMock(
//                    pingSuccess = false, downloadSpeedSuccess = false,
//                    uploadSuccess = false, downloadPlanSuccess = false, downloadModelSuccess = false
//                )
//
//        networkDataSourceFailure = NetworkStatusRealTimeDataSource(
//            httpClientMockFailure.getMockedClient().apiClient,
//            file.parentFile!!,
//            mock(),
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        )
//    }
//
//    @Test
//    fun `updatePing return success response from server`() {
//        networkDataSourceSuccess.updatePing(workerId, networkStatusModel)
//                .blockingAwait()
//        assert(networkStatusModel.ping!!.toInt() >= 3000)
//    }
//
//    @Test
//    fun `updatePing should handle error when request fail`() {
//        networkDataSourceFailure
//                .updatePing(workerId, networkStatusModel)
//                .test()
//                .assertError(NetworkErrorException::class.java)
//    }
//
//    @Test
//    fun `updateDownloadSpeed return success response from server`() {
//        networkDataSourceSuccess
//                .updateDownloadSpeed(workerId, networkStatusModel)
//                .test()
//                .await()
//                .assertComplete()
//    }
//
//    @Test
//    fun `updateDownloadSpeed should handle error when request fail`() {
//        networkDataSourceFailure
//                .updateDownloadSpeed(workerId, networkStatusModel)
//                .test()
//                .assertError(UninitializedPropertyAccessException::class.java)
//    }
//
//    @Test
//    fun `updateUploadSpeed return success response from server`() {
//        networkDataSourceSuccess
//                .updateUploadSpeed(workerId, networkStatusModel).blockingAwait()
//        assert(networkStatusModel.uploadSpeed != null)
//    }
//
//    @Test
//    fun `updateUploadSpeed should handle error when request fail`() {
//        networkDataSourceFailure
//                .updateUploadSpeed(workerId, networkStatusModel)
//                .test()
//                .assertError(NetworkErrorException::class.java)
//    }
//
}
