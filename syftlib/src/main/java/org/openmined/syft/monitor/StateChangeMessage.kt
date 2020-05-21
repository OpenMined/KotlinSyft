package org.openmined.syft.monitor

sealed class StateChangeMessage {
    object Charging : StateChangeMessage()
    object Activity : StateChangeMessage()
    object Network : StateChangeMessage()
}