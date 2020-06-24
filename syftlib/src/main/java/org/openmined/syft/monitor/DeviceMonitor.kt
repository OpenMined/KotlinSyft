package org.openmined.syft.monitor

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.battery.BatteryStatusRepository
import org.openmined.syft.monitor.network.NetworkStatusRepository
import org.openmined.syft.threading.ProcessSchedulers
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "device monitor"

@ExperimentalUnsignedTypes
class DeviceMonitor(
    private val networkStatusRepository: NetworkStatusRepository,
    private val batteryStatusRepository: BatteryStatusRepository,
    private val processSchedulers: ProcessSchedulers
) : Disposable {

    companion object {
        fun construct(
            syftConfig: SyftConfiguration
        ): DeviceMonitor {
            return DeviceMonitor(
                NetworkStatusRepository.initialize(syftConfig),
                BatteryStatusRepository.initialize(syftConfig),
                syftConfig.networkingSchedulers
            )
        }
    }


    private val isDisposed = AtomicBoolean(false)

    private val networkValidity = AtomicBoolean(true)
    private val batteryValidity = AtomicBoolean(true)
    private val userValidity = AtomicBoolean(true)

    private val compositeDisposable = CompositeDisposable()

    init {
        subscribe()
    }

    fun isNetworkStateValid(): Boolean {
        if (isDisposed.get())
            subscribe()
        return networkValidity.get()
    }

    fun isBatteryStateValid(): Boolean {
        if (isDisposed.get())
            subscribe()
        return batteryValidity.get()
    }

    fun isActivityStateValid(): Boolean {
        if (isDisposed.get())
            subscribe()
        return userValidity.get()
    }

    fun getNetworkStatus(workerId: String) = networkStatusRepository.getNetworkStatus(workerId)
    fun getBatteryStatus() = batteryStatusRepository.getBatteryState()

    private fun subscribe() {
        val statusListener = networkStatusRepository.subscribeStateChange()
                .mergeWith(batteryStatusRepository.subscribeStateChange())

        compositeDisposable.add(
            statusListener
                    .subscribeOn(processSchedulers.computeThreadScheduler)
                    .observeOn(processSchedulers.calleeThreadScheduler)
                    .subscribe {
                        when (it) {
                            is StateChangeMessage.Charging -> {
                                batteryValidity.set(it.charging)
                                Log.d(TAG, "device charging ${it.charging}")
                            }
                            is StateChangeMessage.NetworkStatus -> {
                                networkValidity.set(it.connected)
                                Log.d(TAG, "connected to valid network ${it.connected}")
                            }
                        }
                    }
        )

        networkValidity.set(networkStatusRepository.getNetworkValidity())
        batteryValidity.set(batteryStatusRepository.getBatteryValidity())
        isDisposed.set(false)
    }

    override fun isDisposed() = isDisposed.get()

    override fun dispose() {
        compositeDisposable.clear()
        if (!isDisposed()) {
            networkStatusRepository.unsubscribeStateChange()
            batteryStatusRepository.unsubscribeStateChange()
            isDisposed.set(true)
            Log.d(TAG, "disposed device monitor")
        } else
            Log.d(TAG,"device monitor already disposed")
    }
}