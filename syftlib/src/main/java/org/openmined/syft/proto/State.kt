package org.openmined.syft.proto

import org.openmined.syftproto.execution.v1.StateOuterClass
import org.openmined.syftproto.execution.v1.StateTensorOuterClass
import org.pytorch.IValue

@ExperimentalUnsignedTypes
data class State(
    val placeholders: List<Placeholder>,
    val syftTensors: MutableList<SyftTensor>
) {
    fun getTorchTensors() = syftTensors.map { it.getTorchTensor() }
    fun getIValueTensorArray() =
            syftTensors.map { IValue.from(it.getTorchTensor()) }.toTypedArray()

    fun serialize(): StateOuterClass.State {
        return StateOuterClass.State.newBuilder().addAllPlaceholders(
            placeholders.map { it.serialize() }
        ).addAllTensors(syftTensors.map {
            StateTensorOuterClass.StateTensor
                    .newBuilder()
                    .setTorchTensor(it.serialize())
                    .build()
        }).build()
    }

    companion object {
        fun deserialize(state: StateOuterClass.State): State {
            val placeholders = state.placeholdersList.map {
                Placeholder.deserialize(it)
            }
            val syftTensors = state.tensorsList.map {
                SyftTensor.deserialize(it.torchTensor)
            }.toMutableList()
            return State(placeholders, syftTensors)
        }
    }
}