package org.openmined.syft.demo.ui

sealed class ProcessState {
    object Hidden : ProcessState()
    object Loading : ProcessState()
}

data class ProcessData(internal val data: List<Float>)
