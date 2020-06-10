package org.openmined.syft.monitor

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import org.junit.jupiter.api.Test
import org.openmined.syft.monitor.battery.BatteryStatusModel
import org.openmined.syft.monitor.battery.BatteryStatusRepository
import org.openmined.syft.monitor.network.NetworkStatusModel
import org.openmined.syft.monitor.network.NetworkStatusRepository

@ExperimentalUnsignedTypes
internal class DeviceMonitorTest {
    private val processor = PublishProcessor.create<StateChangeMessage>()

    private val networkStatusRepository = mock<NetworkStatusRepository> {
        on { subscribeStateChange() }.thenReturn(processor.onBackpressureLatest())
        on { getNetworkStatus("test id") }.thenReturn(
            Single.just(NetworkStatusModel())
        )
    }
    private val batteryStatusRepository = mock<BatteryStatusRepository> {
        on { subscribeStateChange() }.thenReturn(processor.onBackpressureLatest())
        on { getBatteryState() }.thenReturn(
            BatteryStatusModel(
                true,
                80.0f,
                0
            )
        )
    }

    @Test
    fun `device monitor automatically subscribes to statusProcessor on initialization`() {
        DeviceMonitor(networkStatusRepository, batteryStatusRepository)
        verify(networkStatusRepository).subscribeStateChange()
        verify(batteryStatusRepository).subscribeStateChange()
    }

    @Test
    fun `network validity changes when NetworkStatusRepository`() {
        networkStatusRepository.stub {
            on { getNetworkValidity() }.thenReturn(true)
        }
        val deviceMonitor = DeviceMonitor(networkStatusRepository, batteryStatusRepository)
        assert(deviceMonitor.isNetworkStateValid())
        processor.offer(StateChangeMessage.NetworkStatus(false))
        assert(!deviceMonitor.isNetworkStateValid())
        processor.offer(StateChangeMessage.NetworkStatus(true))
        assert(deviceMonitor.isNetworkStateValid())
    }

    @Test
    fun `isDisposed sets to true after disposing DeviceMonitor`() {
        val deviceMonitor = DeviceMonitor(networkStatusRepository, batteryStatusRepository)
        assert(!deviceMonitor.isDisposed)
        deviceMonitor.dispose()
        assert(deviceMonitor.isDisposed)
    }

    @Test
    fun `networkStatusRepository stops registering network changes on dispose`() {
        val deviceMonitor = DeviceMonitor(networkStatusRepository, batteryStatusRepository)
        deviceMonitor.dispose()
        verify(networkStatusRepository).unsubscribeStateChange()
        verify(batteryStatusRepository).unsubscribeStateChange()
    }

    @Test
    fun `device monitor does not subscribe if already listening`() {
        val deviceMonitor = DeviceMonitor(networkStatusRepository, batteryStatusRepository)
        deviceMonitor.isNetworkStateValid()
        verify(networkStatusRepository, times(1)).subscribeStateChange()
        deviceMonitor.isBatteryStateValid()
        verify(batteryStatusRepository, times(1)).subscribeStateChange()
    }

    @Test
    fun `disposed device monitor resubscribes on checking network validity`() {
        val deviceMonitor = DeviceMonitor(networkStatusRepository, batteryStatusRepository)
        deviceMonitor.dispose()
        deviceMonitor.isNetworkStateValid()
        verify(networkStatusRepository, times(2)).subscribeStateChange()
    }

    @Test
    fun `device monitor does not subscribe if already listening to battery`() {
        val deviceMonitor = DeviceMonitor(networkStatusRepository, batteryStatusRepository)
        deviceMonitor.isBatteryStateValid()
        verify(batteryStatusRepository, times(1)).subscribeStateChange()
    }

    @Test
    fun `disposed device monitor resubscribes on checking battery validity`() {
        val deviceMonitor = DeviceMonitor(networkStatusRepository, batteryStatusRepository)
        deviceMonitor.dispose()
        deviceMonitor.isBatteryStateValid()
        verify(batteryStatusRepository, times(2)).subscribeStateChange()
    }

    @Test
    fun `device monitor does not subscribe if already listening to user activity`() {
        val deviceMonitor = DeviceMonitor(networkStatusRepository, batteryStatusRepository)
        deviceMonitor.isActivityStateValid()
        verify(networkStatusRepository, times(1)).subscribeStateChange()
    }

    @Test
    fun `disposed device monitor resubscribes on checking user activity `() {
        val deviceMonitor = DeviceMonitor(networkStatusRepository, batteryStatusRepository)
        deviceMonitor.dispose()
        deviceMonitor.isActivityStateValid()
        verify(networkStatusRepository, times(2)).subscribeStateChange()
    }

    @Test
    fun `check if networkStatus is delegated to networkStatusRepository`() {
        val deviceMonitor = DeviceMonitor(networkStatusRepository, batteryStatusRepository)
        deviceMonitor.getNetworkStatus("test id")
        verify(networkStatusRepository).getNetworkStatus("test id")
    }

    @Test
    fun `check if batteryStatus is delegated to BatteryStatusRepository`() {
        val deviceMonitor = DeviceMonitor(networkStatusRepository, batteryStatusRepository)
        deviceMonitor.getBatteryStatus()
        verify(batteryStatusRepository).getBatteryState()
    }

}