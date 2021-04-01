package org.openmined.syft.domain

interface SyftLogger {
    fun postState(status: String)

    fun postData(result: Float)

    fun postEpoch(epoch: Int)

    fun postLog(message: String)
}