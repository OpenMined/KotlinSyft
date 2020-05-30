package org.openmined.syft.monitor.battery

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import io.reactivex.Flowable
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.BroadCastListener
import org.openmined.syft.monitor.StateChangeMessage

@ExperimentalUnsignedTypes
class BatteryStatusRepository internal constructor(
    private val batteryStatusDataSource: BatteryStatusDataSource
) : BroadCastListener{
    companion object {
        fun initialize(
            configuration: SyftConfiguration,
            receiver: BroadcastReceiver? = null
        ): BatteryStatusRepository {

            val batteryStatus = configuration.context.registerReceiver(
                receiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val batteryStatusDataSource = BatteryStatusDataSource(batteryStatus)
            return BatteryStatusRepository(batteryStatusDataSource)
        }
    }

    fun getBatteryState() = BatteryStatusModel(
        batteryStatusDataSource.checkIfCharging(),
        batteryStatusDataSource.getBatteryLevel(),
        batteryStatusDataSource.chargeType(),
        System.currentTimeMillis()
    )

    override fun subscribeStateChange(): Flowable<StateChangeMessage> {
        TODO("Not yet implemented")
    }

    override fun unsubscribeStateChange() {
        TODO("Not yet implemented")
    }

}