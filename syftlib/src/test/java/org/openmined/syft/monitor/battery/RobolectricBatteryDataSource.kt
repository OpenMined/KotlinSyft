package org.openmined.syft.monitor.battery

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.notNull
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import org.junit.runner.RunWith
import org.openmined.syft.monitor.StateChangeMessage
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadow.api.Shadow
import java.util.concurrent.TimeUnit

@ExperimentalUnsignedTypes
@RunWith(RobolectricTestRunner::class)
class RobolectricBatteryDataSource {

    @Test
    fun `getBatteryLevel returns the battery percentage from intent`() {
        val intent = Shadow.newInstanceOf(Intent::class.java)
        intent.putExtra(BatteryManager.EXTRA_LEVEL, 1000)
        intent.putExtra(BatteryManager.EXTRA_SCALE, 4000)
        val contextMock = mock<Context> {
            on { registerReceiver(eq(null), any()) }
                    .thenReturn(intent)
        }
        val batteryStatusDataSource = BatteryStatusDataSource(contextMock, true)
        assert(batteryStatusDataSource.getBatteryLevel() == 25.0f)
    }

    @Test
    fun `checkIfCharging returns true when charging`() {
        val intent = Shadow.newInstanceOf(Intent::class.java)
        intent.putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING)
        val contextMock = mock<Context> {
            on { registerReceiver(eq(null), any()) }
                    .thenReturn(intent)
        }
        val batteryStatusDataSource = BatteryStatusDataSource(contextMock, true)
        assert(batteryStatusDataSource.checkIfCharging())
    }


    @Test
    fun `checkIfCharging returns false when not charging`() {
        val intent = Shadow.newInstanceOf(Intent::class.java)
        intent.putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_DISCHARGING)
        val contextMock = mock<Context> {
            on { registerReceiver(eq(null), any()) }
                    .thenReturn(intent)
        }
        val batteryStatusDataSource = BatteryStatusDataSource(contextMock, true)
        assert(!batteryStatusDataSource.checkIfCharging())
    }

    @RunWith(RobolectricTestRunner::class)
    class BatteryChangeReceiverTest {

        private val testScheduler = TestScheduler()
        private val statusProcessor = PublishProcessor.create<StateChangeMessage>()
        private val contextMock = mock<Context> {
            on { registerReceiver(eq(null), any()) }
                    .thenReturn(null)
            on { registerReceiver(notNull(), any()) }
                    .thenReturn(null)
        }
        private val batterySource = BatteryStatusDataSource(contextMock, true, statusProcessor)
        private val batteryChangeReceiver = batterySource.BatteryChangeReceiver()

        @Test
        fun `status processor offers battery disconnected message when receiver gets the intent`() {
            val intent = Shadow.newInstanceOf(Intent::class.java)
            intent.action = Intent.ACTION_POWER_DISCONNECTED

            val testReceiver = batterySource.subscribeStateChange()
                    .subscribeOn(testScheduler)
                    .observeOn(testScheduler)
                    .test()
            testScheduler.advanceTimeBy(1L, TimeUnit.MILLISECONDS)
            batteryChangeReceiver.onReceive(null, intent)
            testScheduler.advanceTimeBy(1L, TimeUnit.MILLISECONDS)
            testReceiver.assertValue(StateChangeMessage.Charging(false))
            testReceiver.dispose()
        }

        @Test
        fun `status processor offers battery charging message when receiver gets the intent`() {
            val intent = Shadow.newInstanceOf(Intent::class.java)
            intent.action = Intent.ACTION_POWER_CONNECTED

            val testReceiver = batterySource.subscribeStateChange()
                    .subscribeOn(testScheduler)
                    .observeOn(testScheduler)
                    .test()
            testScheduler.advanceTimeBy(1L, TimeUnit.MILLISECONDS)
            batteryChangeReceiver.onReceive(null, intent)
            testScheduler.advanceTimeBy(1L, TimeUnit.MILLISECONDS)
            testReceiver.assertValue(StateChangeMessage.Charging(true))
            testReceiver.dispose()
        }
    }

}