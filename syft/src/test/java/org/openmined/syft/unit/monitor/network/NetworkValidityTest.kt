package org.openmined.syft.unit.monitor.network

import android.content.Context
import android.net.ConnectivityManager
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import io.reactivex.Maybe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.openmined.syft.common.AbstractSyftWorkerTest
import org.openmined.syft.monitor.network.NetworkStatusCache
import org.openmined.syft.monitor.network.NetworkStatusModel
import org.openmined.syft.monitor.network.NetworkStatusRealTimeDataSource
import org.openmined.syft.monitor.network.NetworkStatusRepository
import org.openmined.syft.unit.monitor.network.client.DelayedHttpClientMock

@ExperimentalUnsignedTypes
class NetworkValidityTest : AbstractSyftWorkerTest() {

//    @Rule
//    @JvmField
//    var tempFolder = TemporaryFolder()
//    private lateinit var networkDataSource: NetworkStatusRealTimeDataSource
//
//    @Before
//    fun setUp() {
//        val file = tempFolder.newFile("uploadFile")
//
//        val httpClient = DelayedHttpClientMock(
//            pingSuccess = true, downloadSpeedSuccess = true,
//            uploadSuccess = true, downloadPlanSuccess = true, downloadModelSuccess = true
//        )
//
//        networkDataSource = NetworkStatusRealTimeDataSource(
//            httpClient.getMockedClient().apiClient,
//            file.parentFile!!,
//            mock(),
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        )
//
//    }
//
//    @Test
//    fun `network validity should return true if network constraints satisfied`() {
//        assert(networkDataSource.getNetworkValidity(networkConstraints))
//    }
//
//    @Test(expected = Exception::class)
//    fun `network validity should throw exception if constraints failed`() {
//        val manager = mock<ConnectivityManager> {
//            on { getNetworkCapabilities(it.activeNetwork) }.doReturn(null)
//        }
//
//        val networkDataSource = NetworkStatusRealTimeDataSource(
//            mock(),
//            mock(),
//            mock(),
//            manager
//        )
//
//        networkDataSource.getNetworkValidity(networkConstraints)
//    }
//
//    // Test for NetworkRepository's getNetworkStatus, require using AbstractSyftWorkerTest
//    @Test
//    fun `getNetworkStatus calculate new NetworkModelStatus if there no cache`() {
//        val cache = mock<NetworkStatusCache> {
//            on { getNetworkStatusCache() }.thenReturn(Maybe.empty())
//        }
//
//        val networkStatusRepository = NetworkStatusRepository(listOf(), cache, networkDataSource)
//        val cachedStatus = networkStatusRepository.getNetworkStatus("worker id", true).blockingGet()
//
//        assert(cachedStatus.ping!!.toInt() >= 3000)
//        assert(cachedStatus.downloadSpeed != null)
//        assert(cachedStatus.uploadSpeed != null)
//        assert(cachedStatus.networkValidity)
//    }
//
//    // Test for NetworkRepository's getNetworkStatus
//    @Test
//    fun `getNetworkStatus should return cached network status model`() {
//        val networkStatusModel = NetworkStatusModel(100, 100f, 100f, true)
//        val cache = mock<NetworkStatusCache> {
//            on { networkStateCache }.thenReturn(networkStatusModel)
//            on { getNetworkStatusCache() }.thenReturn(Maybe.just(networkStatusModel))
//        }
//
//        val networkStatusRepository = NetworkStatusRepository(listOf(), cache, networkDataSource)
//        val cachedStatus = networkStatusRepository.getNetworkStatus("worker id", true).blockingGet()
//
//        assert(cachedStatus.ping!! >= 100)
//        assert(cachedStatus.downloadSpeed!! >= 100f)
//        assert(cachedStatus.uploadSpeed!! >= 100f)
//        assert(cachedStatus.networkValidity)
//    }
//
}
