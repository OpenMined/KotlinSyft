package org.openmined.syft.demo.federated.ui

import androidx.lifecycle.MutableLiveData

class Logger {
    companion object {
        private var INSTANCE: Logger? = null

        fun getInstance(): Logger {
            return Logger.INSTANCE ?: synchronized(this) {
                Logger.INSTANCE ?: Logger().also { Logger.INSTANCE = it }
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
                result
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