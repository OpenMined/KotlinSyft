package org.openmined.syft.monitor.network

import io.reactivex.Completable
import io.reactivex.Single
import org.openmined.syft.domain.SyftConfiguration

private const val TAG = "NetworkStateRepository"


@ExperimentalUnsignedTypes
class NetworkStatusRepository(private val configuration: SyftConfiguration)  {
    private val cacheService = NetworkStatusCache(configuration.cacheTimeOut)
    private val realTimeDataService = NetworkStatusRealTimeDataSource(configuration)

    fun getNetworkStatus(workerId: String): Single<NetworkStatusModel> {
        return cacheService.getNetworkStatusCache()
                .switchIfEmpty(getNetworkStatusUncached(workerId))
    }

    private fun getNetworkStatusUncached(workerId: String): Single<NetworkStatusModel> {
        val networkStatus = NetworkStatusModel()
        return realTimeDataService.updatePing(workerId, networkStatus)
                .andThen(realTimeDataService.updateDownloadSpeed(workerId, networkStatus))
                .andThen(realTimeDataService.updateUploadSpeed(workerId, networkStatus))
                .andThen(Completable.create {
                    realTimeDataService.updateNetworkValidity(
                        configuration.networkConstraints,
                        networkStatus
                    )
                    networkStatus.cacheTimeStamp = System.currentTimeMillis()
                    cacheService.networkStateCache = networkStatus
                })
                .andThen(Single.just(cacheService.networkStateCache))
    }


}