package org.openmined.syft.monitor.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.BroadCastListener
import org.openmined.syft.monitor.StateChangeMessage

@ExperimentalUnsignedTypes
class BatteryStatusDataSource(
    private val context: Context,
    private val statusProcessor: PublishProcessor<StateChangeMessage> = PublishProcessor.create()
) : BroadCastListener {

    companion object {
        fun initialize(configuration: SyftConfiguration): BatteryStatusDataSource {
            return BatteryStatusDataSource(configuration.context)
        }
    }

    private val batteryStatusIntent = context.registerReceiver(
        null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    private val broadcastReceiver: BatteryChangeReceiver = BatteryChangeReceiver()


    fun getBatteryLevel(): Float? {
        return batteryStatusIntent?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
    }

    fun checkIfCharging(): Boolean {
        val charge = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return charge == BatteryManager.BATTERY_STATUS_CHARGING
               || charge == BatteryManager.BATTERY_STATUS_FULL
    }

    override fun subscribeStateChange(): Flowable<StateChangeMessage> {
        context.registerReceiver(
            broadcastReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        return statusProcessor.onBackpressureLatest()
    }

    override fun unsubscribeStateChange() {
        context.unregisterReceiver(broadcastReceiver)
        statusProcessor.onComplete()
    }

    inner class BatteryChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_POWER_DISCONNECTED ->
                    statusProcessor.offer(StateChangeMessage.Charging(false))
                Intent.ACTION_POWER_CONNECTED ->
                    statusProcessor.offer(StateChangeMessage.Charging(true))
            }
        }

    }
}