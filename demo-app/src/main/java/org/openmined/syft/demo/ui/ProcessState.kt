package org.openmined.syft.demo.ui

sealed class ProcessState {
    object Hidden : ProcessState()
    object Loading : ProcessState()
    class ProcessMessage(val message: String) : ProcessState()
}
