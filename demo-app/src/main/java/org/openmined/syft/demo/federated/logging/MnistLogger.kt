package org.openmined.syft.demo.federated.logging

import androidx.lifecycle.MutableLiveData
import org.openmined.syft.demo.federated.ui.ContentState
import org.openmined.syft.demo.federated.ui.ProcessData

abstract class MnistLogger {
    val logText
        get() = logTextInternal
    protected val logTextInternal = MutableLiveData<String>()

    val steps
        get() = stepsInternal
    protected val stepsInternal = MutableLiveData<String>()

    val processState
        get() = processStateInternal
    protected val processStateInternal = MutableLiveData<ContentState>()

    val processData
        get() = processDataInternal
    protected val processDataInternal = MutableLiveData<ProcessData>()

    abstract fun postState(status: ContentState)

    abstract fun postData(result: Float)

    abstract fun postEpoch(epoch: Int)

    abstract fun postLog(message: String)
}