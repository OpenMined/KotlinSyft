package org.openmined.syft.monitor.network

import io.reactivex.Flowable
import io.reactivex.Single
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.BroadCastListener
import org.openmined.syft.monitor.StateChangeMessage

private const val TAG = "NetworkStateRepository"


@ExperimentalUnsignedTypes
internal class NetworkStatusRepository (
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

    fun getNetworkStatus(workerId: String, requiresSpeedTest: Boolean): Single<NetworkStatusModel> {
        return if (requiresSpeedTest) {
            cacheService.getNetworkStatusCache()
                    .switchIfEmpty(getNetworkStatusUncached(workerId))
        } else {
            Single.just(NetworkStatusModel("", "", ""))
        }
    }

    fun getNetworkValidity() = realTimeDataService.getNetworkValidity(networkConstraints)


    override fun subscribeStateChange(): Flowable<StateChangeMessage> =
            realTimeDataService.subscribeStateChange()

    override fun unsubscribeStateChange() {
        realTimeDataService.unsubscribeStateChange()
    }

    private fun getNetworkStatusUncached(workerId: String): Single<NetworkStatusModel> {
        val networkStatus = NetworkStatusModel()
        // TODO Must ping be excluded if requiresSpeedTest is false?
        return realTimeDataService.updatePing(workerId, networkStatus)
                .andThen(realTimeDataService.updateDownloadSpeed(workerId, networkStatus))
                .andThen(realTimeDataService.updateUploadSpeed(workerId, networkStatus))
                .andThen(Single.create {
                    networkStatus.networkValidity = realTimeDataService.getNetworkValidity(
                        networkConstraints
                    )
                    networkStatus.cacheTimeStamp = System.currentTimeMillis()
                    cacheService.networkStateCache = networkStatus
                    it.onSuccess(networkStatus)
                })
    }

}
