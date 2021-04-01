package org.openmined.syft.monitor.network

import io.reactivex.Flowable
import io.reactivex.Single
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.BroadCastListener
import org.openmined.syft.monitor.StateChangeMessage

private const val TAG = "NetworkStateRepository"


@ExperimentalUnsignedTypes
internal class NetworkStatusRepository(
    private val networkConstraints: List<Int>,
    private val cacheService: NetworkStatusCache,
    private val realTimeDataService: NetworkStatusRealTimeDataSource
) : BroadCastListener {

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

    suspend fun getNetworkStatus(workerId: String, requiresSpeedTest: Boolean): NetworkStatusModel {
        return if (requiresSpeedTest) {
            cacheService.getNetworkStatusCache() ?: getNetworkStatusUncached(workerId)
        } else {
            // TODO Maybe create an InvalidNetworkStatus?
            NetworkStatusModel(-1, 0.0f, 0.0f)
        }
    }

    fun getNetworkValidity() = realTimeDataService.getNetworkValidity(networkConstraints)


    override fun subscribeStateChange(): Flowable<StateChangeMessage> =
            realTimeDataService.subscribeStateChange()

    override fun unsubscribeStateChange() {
        realTimeDataService.unsubscribeStateChange()
    }

    private suspend fun getNetworkStatusUncached(workerId: String): NetworkStatusModel {
        // TODO Must ping be excluded if requiresSpeedTest is false?
        val ping = realTimeDataService.updatePing(workerId)

        val downloadSpeed = realTimeDataService.updateDownloadSpeed(workerId)
        val uploadSpeed = realTimeDataService.updateUploadSpeed(workerId)

        val networkValidity = realTimeDataService.getNetworkValidity(
            networkConstraints
        )
        val networkStatus = NetworkStatusModel(ping, downloadSpeed, uploadSpeed, networkValidity, System.currentTimeMillis())
        cacheService.networkStateCache = networkStatus
        return networkStatus
    }

}
