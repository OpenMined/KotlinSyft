package org.openmined.syft.monitor

sealed class StateChangeMessage {
    data class Charging(val charging: Boolean) : StateChangeMessage()
    data class NetworkStatus(val connected: Boolean) : StateChangeMessage()
}