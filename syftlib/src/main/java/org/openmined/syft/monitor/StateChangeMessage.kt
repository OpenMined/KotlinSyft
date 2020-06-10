package org.openmined.syft.monitor

sealed class StateChangeMessage {
    class Charging(val charging: Boolean) : StateChangeMessage()
    object Activity : StateChangeMessage()
    class NetworkStatus(val connected: Boolean) : StateChangeMessage()
}