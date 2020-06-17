package org.openmined.syft.unit.monitor.battery

import android.content.Context
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.notNull
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import org.junit.Test
import org.openmined.syft.monitor.StateChangeMessage
import org.openmined.syft.monitor.battery.BatteryStatusDataSource

@ExperimentalUnsignedTypes
class BatteryStatusDataSourceTest {

    @Test
    fun `battery validity always returns true if check is disabled`() {
        val contextMock = mock<Context>()
        val batteryStatusDataSource = spy(BatteryStatusDataSource(contextMock, false)) {
            on { checkIfCharging() }.thenReturn(false)
        }
        val batteryStat = batteryStatusDataSource.getBatteryValidity()
        assert(batteryStat)
        verify(batteryStatusDataSource, never()).checkIfCharging()
    }

    @Test
    fun `battery validity calls checkIfCharging if check is enabled`() {
        val contextMock = mock<Context>()
        val batteryStatusDataSource = spy(BatteryStatusDataSource(contextMock, true)) {
            on { checkIfCharging() }.thenReturn(false)
        }
        val batteryStat = batteryStatusDataSource.getBatteryValidity()
        assert(!batteryStat)
        verify(batteryStatusDataSource).checkIfCharging()
    }

    @Test
    fun `subscribeStateChange registers a non null receiver if battery check is enabled`() {
        val contextMock = mock<Context> {
            on { registerReceiver(eq(null), any()) }
                    .thenReturn(null)
        }
        val batteryStatusDataSource = BatteryStatusDataSource(contextMock, true)
        batteryStatusDataSource.subscribeStateChange()
        verify(contextMock, times(1)).registerReceiver(eq(null), any())
        verify(contextMock, times(1)).registerReceiver(notNull(), any())
    }

    @Test
    fun `unSubscribeStateChange de-registers a non null receiver if battery check is enabled`() {
        val contextMock = mock<Context> {
            on { registerReceiver(eq(null), any()) }
                    .thenReturn(null)
        }
        val batteryStatusDataSource = BatteryStatusDataSource(contextMock, true)
        batteryStatusDataSource.unsubscribeStateChange()
        verify(contextMock, times(1)).unregisterReceiver(notNull())
    }

    @Test
    fun `check constructor status processor is returned on subscribe`() {
        val contextMock = mock<Context> {
            on { registerReceiver(eq(null), any()) }
                    .thenReturn(null)
        }
        val flowable = mock<Flowable<StateChangeMessage>>()
        val statusProcessor = mock<PublishProcessor<StateChangeMessage>>{
            on { onBackpressureLatest() }.thenReturn(flowable)
        }
        val batteryStatusDataSource = BatteryStatusDataSource(contextMock, false, statusProcessor)
        assert(batteryStatusDataSource.subscribeStateChange() == flowable)
    }

    @Test
    fun `check status processor is completed on unSubscribe`() {
        val contextMock = mock<Context> {
            on { registerReceiver(eq(null), any()) }
                    .thenReturn(null)
        }
        val statusProcessor = mock<PublishProcessor<StateChangeMessage>>()
        val batteryStatusDataSource = BatteryStatusDataSource(contextMock, false, statusProcessor)
        batteryStatusDataSource.unsubscribeStateChange()
        verify(statusProcessor).onComplete()
    }
}