package org.openmined.syft.unit.monitor.battery

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.openmined.syft.monitor.battery.BatteryStatusDataSource
import org.openmined.syft.monitor.battery.BatteryStatusRepository

@ExperimentalUnsignedTypes
class BatteryStatusRepositoryTest {

    private val batteryLevel = 100f
    private val batteryStatusDataSource = mock<BatteryStatusDataSource> {
        on { getBatteryLevel() }.thenReturn(batteryLevel)
        on { getBatteryValidity() }.thenReturn(true)
        on { checkIfCharging() }.thenReturn(false)
    }

    private val repository = BatteryStatusRepository(batteryStatusDataSource)

    @Test
    fun `verify calling repository getBatteryValidity will call dataSource getBatteryValidity`() {
        val validity = repository.getBatteryValidity()
        assert(validity)
        verify(batteryStatusDataSource, times(1)).getBatteryValidity()
    }

    @Test
    fun `verify subscribe to state change will call subscribe on dataSource`() {
        repository.subscribeStateChange()
        verify(batteryStatusDataSource, times(1)).subscribeStateChange()
    }

    @Test
    fun `verify unsubscribe from state change will call unsubscribe on dataSource`() {
        repository.unsubscribeStateChange()
        verify(batteryStatusDataSource, times(1)).unsubscribeStateChange()
    }

    @Test
    fun `verify get battery state will return battery status model`() {
        val model = repository.getBatteryState()
        assert(!model.isCharging)
        assert(model.batteryLevel == batteryLevel)
        assert(model.cacheTimeStamp > 0)
    }

}