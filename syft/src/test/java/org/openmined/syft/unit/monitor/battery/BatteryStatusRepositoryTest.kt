package org.openmined.syft.unit.monitor.battery

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.openmined.syft.monitor.battery.BatteryStatusDataSource
import org.openmined.syft.monitor.battery.BatteryStatusRepository

@ExperimentalUnsignedTypes
class BatteryStatusRepositoryTest {

    private val repository = BatteryStatusRepository(mock())
    private val batteryStatusDataSource = mock<BatteryStatusDataSource>()

    @Test
    fun `verify calling repository getBatteryValidity will call dataSource getBatteryValidity`() {
        repository.getBatteryValidity()
        verify(batteryStatusDataSource).getBatteryValidity()
    }

}