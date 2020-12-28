package org.openmined.syft.demo.federated.logging

import androidx.lifecycle.MutableLiveData
import org.openmined.syft.demo.federated.ui.ContentState
import org.openmined.syft.domain.ProcessData
import org.openmined.syft.domain.SyftLogger

interface MnistLogger : SyftLogger {
    val logText: MutableLiveData<String>

    val steps: MutableLiveData<String>

    val processState: MutableLiveData<ContentState>

    val processData: MutableLiveData<ProcessData>

    fun postState(status: ContentState)

    override fun postState(status: String)

    override fun postData(result: Float)

    override fun postEpoch(epoch: Int)

    override fun postLog(message: String)
}