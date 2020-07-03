package org.openmined.syft.demo.federated.ui

import androidx.lifecycle.MutableLiveData

class MnistLogger {
    companion object {
        private var INSTANCE: MnistLogger? = null

        fun getInstance(): MnistLogger {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MnistLogger().also { INSTANCE = it }
            }
        }
    }

    val logText
        get() = _logText
    private val _logText = MutableLiveData<String>()

    val steps
        get() = _steps
    private val _steps = MutableLiveData<String>()

    val processState
        get() = _processState
    private val _processState = MutableLiveData<ContentState>()

    val processData
        get() = _processData
    private val _processData = MutableLiveData<ProcessData>()

    fun postState(state: ContentState) {
        _processState.postValue(state)
    }

    fun postData(result: List<Float>) {
        _processData.postValue(
            ProcessData(
                (_processData.value?.data ?: emptyList()) + result
            )
        )
    }

    fun postEpoch(epoch: Int) {
        _steps.postValue("Step : $epoch")
    }

    fun postLog(message: String) {
        _logText.postValue("${_logText.value ?: ""}\n\n$message")
    }
}