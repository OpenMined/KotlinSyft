package org.openmined.syft.monitor.network

import android.content.Context
import android.net.ConnectivityManager
import io.reactivex.Completable
import io.reactivex.Single
import org.openmined.syft.domain.SyftConfiguration

private const val TAG = "NetworkStateRepository"


@ExperimentalUnsignedTypes
class NetworkStatusRepository internal constructor(
    private val networkConstraints: List<Int>,
    private val cacheService: NetworkStatusCache,
    private val realTimeDataService: NetworkStatusRealTimeDataSource
) {
    companion object {
        fun initialize(
            configuration: SyftConfiguration
        ): NetworkStatusRepository {
            val cacheService = NetworkStatusCache(configuration.cacheTimeOut)
            val networkManager = configuration.context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val realTimeDataService = NetworkStatusRealTimeDataSource(
                configuration.getDownloader(),
                configuration.filesDir,
                networkManager
            )
            return NetworkStatusRepository(
                configuration.networkConstraints,
                cacheService,
                realTimeDataService
            )
        }
    }

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
                        networkConstraints,
                        networkStatus
                    )
                    networkStatus.cacheTimeStamp = System.currentTimeMillis()
                    cacheService.networkStateCache = networkStatus
                })
                .andThen(Single.just(cacheService.networkStateCache))
    }


}