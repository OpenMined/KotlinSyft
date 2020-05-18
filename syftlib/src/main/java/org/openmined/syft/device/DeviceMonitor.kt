package org.openmined.syft.device

import io.reactivex.Completable
import io.reactivex.Single
import org.openmined.syft.device.models.BatteryStatusModel
import org.openmined.syft.device.models.NetworkStateModel
import org.openmined.syft.device.repositories.BatteryStatusRepository
import org.openmined.syft.device.repositories.NetworkStatusRepository
import org.openmined.syft.domain.SyftConfiguration

@ExperimentalUnsignedTypes
class DeviceMonitor(
    private val syftConfig: SyftConfiguration,
    private val cacheTimeOut: Long
) {

    private val networkStatusRepository = NetworkStatusRepository(syftConfig)
    private val batteryStatusRepository = BatteryStatusRepository(syftConfig.context)
    private val networkState = NetworkStateModel()
    private val batteryState = getBatteryState()

    fun checkValidity(): Boolean {
        var valid = true
        if (isCacheValid(networkState.cacheTimeStamp))
            valid = valid.and(networkState.networkValidity)
        return valid
    }

    fun getNetworkState(workerId: String): Single<NetworkStateModel> {
        return networkStatusRepository.updatePing(workerId, networkState)
                .andThen(networkStatusRepository.updateDownloadSpeed(workerId, networkState))
                .andThen(networkStatusRepository.updateUploadSpeed(workerId, networkState))
                .andThen(Completable.create {
                    networkStatusRepository.updateNetworkValidity(
                        syftConfig.networkConstraints,
                        networkState
                    )
                    networkState.cacheTimeStamp = System.currentTimeMillis()
                })
                .andThen(Single.just(networkState))
    }


    fun getBatteryState() = BatteryStatusModel(
        batteryStatusRepository.checkIfCharging(),
        batteryStatusRepository.getBatteryLevel(),
        batteryStatusRepository.chargeType(),
        System.currentTimeMillis()
    )

    private fun isCacheValid(timestamp: Long) =
            System.currentTimeMillis() - timestamp < cacheTimeOut

}