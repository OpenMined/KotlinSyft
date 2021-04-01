package org.openmined.syft.monitor.network

internal class NetworkStatusCache(private val cacheTimeOut: Long) {

    var networkStateCache = NetworkStatusModel()

    fun getNetworkStatusCache(): NetworkStatusModel? {
        return if (isCacheValid())
            networkStateCache
        else
            // TODO Better return an InvalidNetworkStatus
            null
    }

    // TODO This is not used anywhere. If still required, set it as a property independent of NetworkStatusModel
    fun setCacheInvalid() {
        networkStateCache = networkStateCache.copy(networkValidity = false)
    }

    private fun isCacheValid(): Boolean {
        val cacheTimeValidity =
                System.currentTimeMillis() - networkStateCache.cacheTimeStamp < cacheTimeOut
        val fieldValidity = (networkStateCache.downloadSpeed != null)
                            && (networkStateCache.ping != null)
                            && (networkStateCache.uploadSpeed != null)
        return cacheTimeValidity and fieldValidity and networkStateCache.networkValidity
    }

}