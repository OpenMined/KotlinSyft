package org.openmined.syft.execution

sealed class TrainingState {

    data class Message(val message: String) : TrainingState()

    data class Epoch(val epoch: Int) : TrainingState()

    data class Error(val throwable: Throwable) : TrainingState()

    data class Data(val result: Float) : TrainingState()

    object Complete : TrainingState()
}