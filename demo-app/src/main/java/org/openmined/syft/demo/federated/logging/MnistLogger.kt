package org.openmined.syft.demo.federated.logging

import androidx.lifecycle.MutableLiveData
import org.openmined.syft.demo.federated.ui.ContentState
import org.openmined.syft.demo.federated.ui.ProcessData

interface MnistLogger {
    val logText: MutableLiveData<String>

    val steps: MutableLiveData<String>

    val processState: MutableLiveData<ContentState>

    val processData: MutableLiveData<ProcessData>

    fun postState(status: ContentState)

    fun postData(result: Float)

    fun postEpoch(epoch: Int)

    fun postLog(message: String)
}