package org.openmined.syft.unit.monitor.network

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Flowable
import org.junit.Test
import org.openmined.syft.monitor.StateChangeMessage
import org.openmined.syft.monitor.network.NetworkStatusRealTimeDataSource
import org.openmined.syft.monitor.network.NetworkStatusRepository

@ExperimentalUnsignedTypes
class NetworkStatusRepositoryTest {

//    @Test
//    fun `NetworkStatusRepository subscribe successfully`() {
//        val networkStatusRealDataSource = mock<NetworkStatusRealTimeDataSource> {
//            on { subscribeStateChange() }.thenReturn(Flowable.just(StateChangeMessage.NetworkStatus(true)))
//        }
//
//        val networkStatusRepository = NetworkStatusRepository(listOf(), mock(), networkStatusRealDataSource)
//        networkStatusRepository.subscribeStateChange().test().hasSubscription()
//        verify(networkStatusRealDataSource).subscribeStateChange()
//    }
//
//    @Test
//    fun `NetworkStatusRepository unsubscribe successfully`() {
//        val networkStatusRealDataSource = mock<NetworkStatusRealTimeDataSource> {
//            on { subscribeStateChange() }.thenReturn(Flowable.just(StateChangeMessage.NetworkStatus(true)))
//        }
//
//        val networkStatusRepository = NetworkStatusRepository(listOf(), mock(), networkStatusRealDataSource)
//        networkStatusRepository.unsubscribeStateChange()
//        verify(networkStatusRealDataSource).unsubscribeStateChange()
//    }
//
//    @Test
//    fun `network validity call NetworkStatusDataSource's getNetworkValidity correctly`() {
//        val networkStatusRealDataSource = mock<NetworkStatusRealTimeDataSource> {
//            on { getNetworkValidity(listOf()) }.thenReturn(true)
//        }
//        val networkStatusRepository = NetworkStatusRepository(listOf(), mock(), networkStatusRealDataSource)
//        networkStatusRepository.getNetworkValidity()
//        verify(networkStatusRealDataSource).getNetworkValidity(listOf())
//    }
//
//    @Test
//    fun `network status should return new network status model if requiresSpeedTest is false`() {
//        val networkStatusRealDataSource = mock<NetworkStatusRealTimeDataSource> {
//            on { getNetworkValidity(listOf()) }.thenReturn(true)
//        }
//        val networkStatusRepository = NetworkStatusRepository(listOf(), mock(), networkStatusRealDataSource)
//        val model = networkStatusRepository.getNetworkStatus("worker id", false).blockingGet()
//
//        println("Data ${model.ping!!}")
//        println("Data ${model.downloadSpeed!!}")
//        println("Data ${model.uploadSpeed!!}")
//        println("Data ${!model.networkValidity}")
//
//        assert(model.ping!! <= 0)
//        assert(model.downloadSpeed!! <= 0f)
//        assert(model.uploadSpeed!! <= 0f)
//        assert(!model.networkValidity)
//    }

}