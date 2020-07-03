package org.openmined.syft.demo.federated.ui.logging

import org.openmined.syft.demo.federated.ui.ContentState
import org.openmined.syft.demo.federated.ui.ProcessData

class ActivityLogger : MnistLogger() {
    companion object {
        private var INSTANCE: ActivityLogger? = null

        fun getInstance(): ActivityLogger {
            return INSTANCE
                   ?: synchronized(this) {
                       INSTANCE
                       ?: ActivityLogger()
                               .also { INSTANCE = it }
                   }
        }
    }

    override fun postState(status: ContentState) {
        processStateInternal.postValue(status)
    }

    override fun postData(result: List<Float>) {
        processDataInternal.postValue(
            ProcessData(
                (processDataInternal.value?.data ?: emptyList()) + result
            )
        )
    }

    override fun postEpoch(epoch: Int) {
        stepsInternal.postValue("Step : $epoch")
    }

    override fun postLog(message: String) {
        logTextInternal.postValue("${logTextInternal.value ?: ""}\n\n$message")
    }
}