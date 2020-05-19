package org.openmined.syft.monitor.network

import io.reactivex.Maybe

class NetworkStatusCache(private val cacheTimeOut: Long) {

    var networkStateCache = NetworkStatusModel()
    fun getNetworkStatusCache(): Maybe<NetworkStatusModel> {
        return if (isCacheValid())
            Maybe.just(networkStateCache)
        else
            Maybe.empty()
    }

    private fun isCacheValid(): Boolean {
        val cacheTimeValidity =
                System.currentTimeMillis() - networkStateCache.cacheTimeStamp < cacheTimeOut
        val fieldValidity = (networkStateCache.downloadSpeed != null)
                            && (networkStateCache.ping != null)
                            && (networkStateCache.uploadspeed != null)
        return cacheTimeValidity and fieldValidity and networkStateCache.networkValidity
    }

}