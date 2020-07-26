package org.openmined.syft.unit.monitor.network

import org.junit.Test
import org.openmined.syft.monitor.network.NetworkStatusCache
import org.openmined.syft.monitor.network.NetworkStatusModel

@ExperimentalUnsignedTypes
class NetworkStatusCacheTest {

    @Test
    fun `invalidate network cache set networkValidity attribute to false`() {
        val networkStatusCache = NetworkStatusCache(5)
        assert(!networkStatusCache.networkStateCache.networkValidity)

        // Simulate networkValidity is true
        networkStatusCache.networkStateCache.networkValidity = true
        networkStatusCache.setCacheInvalid()
        assert(!networkStatusCache.networkStateCache.networkValidity)
    }

    @Test
    fun `getNetworkStatusCache should return empty NetworkModelState`() {
        val networkStatusCache = NetworkStatusCache(5)
        assert(networkStatusCache.getNetworkStatusCache().isEmpty.blockingGet())
        assert(!networkStatusCache.networkStateCache.networkValidity)
    }

    @Test
    fun `getNetworkStatusCache should return non-empty NetworkModelState`() {
        val networkStatusCache = NetworkStatusCache(5)
        val networkStatusModel = NetworkStatusModel(100, 100f, 100f, true)
        networkStatusCache.networkStateCache = networkStatusModel

        assert(!networkStatusCache.getNetworkStatusCache().isEmpty.blockingGet())
        val cache = networkStatusCache.getNetworkStatusCache().blockingGet()
        assert(cache.ping!! >= 100)
        assert(cache.downloadSpeed!! >= 100)
        assert(cache.uploadSpeed!! >= 100)
        assert(cache.networkValidity)
    }

}