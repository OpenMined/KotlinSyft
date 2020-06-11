package org.openmined.syft.demo.federated.ui

sealed class ContentState {
    object Training : ContentState()
    object Loading : ContentState()
}

data class ProcessData(internal val data: List<Float>)
