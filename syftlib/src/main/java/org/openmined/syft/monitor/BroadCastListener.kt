package org.openmined.syft.monitor

interface BroadCastListener {
    fun registerListener()
    fun deregisterListener()
}