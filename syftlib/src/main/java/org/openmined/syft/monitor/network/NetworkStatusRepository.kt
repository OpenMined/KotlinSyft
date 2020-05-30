package org.openmined.syft.monitor.network

import io.reactivex.Flowable
import io.reactivex.Single
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.BroadCastListener
import org.openmined.syft.monitor.StateChangeMessage

private const val TAG = "NetworkStateRepository"


@ExperimentalUnsignedTypes
class NetworkStatusRepository internal constructor(
    private val networkConstraints: List<Int>,
    private val cacheService: NetworkStatusCache,
    private val realTimeDataService: NetworkStatusRealTimeDataSource
) : BroadCastListener{

    companion object {
        fun initialize(
            configuration: SyftConfiguration
        ): NetworkStatusRepository {
            val cacheService = NetworkStatusCache(configuration.cacheTimeOut)
            val realTimeDataService = NetworkStatusRealTimeDataSource.initialize(configuration)

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

    override fun subscribeStateChange(): Flowable<StateChangeMessage> =
            realTimeDataService.subscribeStateChange()

    override fun unsubscribeStateChange() {
        realTimeDataService.unsubscribeStateChange()
    }

    private fun getNetworkStatusUncached(workerId: String): Single<NetworkStatusModel> {
        val networkStatus = NetworkStatusModel()
        return realTimeDataService.updatePing(workerId, networkStatus)
                .andThen(realTimeDataService.updateDownloadSpeed(workerId, networkStatus))
                .andThen(realTimeDataService.updateUploadSpeed(workerId, networkStatus))
                .andThen(Single.create {
                    realTimeDataService.updateNetworkValidity(
                        networkConstraints,
                        networkStatus
                    )
                    networkStatus.cacheTimeStamp = System.currentTimeMillis()
                    cacheService.networkStateCache = networkStatus
                    it.onSuccess(networkStatus)
                })
    }

}
