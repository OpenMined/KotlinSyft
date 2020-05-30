package org.openmined.syft.monitor

import org.openmined.syft.monitor.battery.CHARGE_TYPE

sealed class StateChangeMessage {
    class Charging(val chargingState: CHARGE_TYPE) : StateChangeMessage()
    object Activity : StateChangeMessage()
    class NetworkStatus(val connected: Boolean) : StateChangeMessage()
}