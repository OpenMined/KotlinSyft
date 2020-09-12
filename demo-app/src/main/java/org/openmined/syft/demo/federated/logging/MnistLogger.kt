package org.openmined.syft.demo.federated.logging

import androidx.lifecycle.MutableLiveData
import org.openmined.syft.domain.ContentState
import org.openmined.syft.domain.ProcessData
import org.openmined.syft.domain.SyftLogger

interface MnistLogger : SyftLogger {
    val logText: MutableLiveData<String>

    val steps: MutableLiveData<String>

    val processState: MutableLiveData<ContentState>

    val processData: MutableLiveData<ProcessData>
}