package org.openmined.syft.proto

import org.openmined.syftproto.execution.v1.StateOuterClass
import org.openmined.syftproto.execution.v1.StateTensorOuterClass
import org.pytorch.IValue

// State class is responsible for storing all the weights of the neural network.
// We update these model weights after every plan.execute
// For usage check SyftModel.updateModel and SyftModel.loadModelState
@ExperimentalUnsignedTypes
data class State(
    val placeholders: List<Placeholder>,
    val syftTensors: MutableList<SyftTensor>
) {
    // Return a list of TorchTensors from a list of SyftTensors
    fun getTorchTensors() = syftTensors.map { it.getTorchTensor() }

    // Return an array of pytorch IValue from the SyftTensors list
    // for more information on IValue(Interpreter Value) check:
    // https://pytorch.org/cppdocs/api/structc10_1_1_i_value.html#_CPPv4N3c106IValueE
    fun getIValueTensorArray() =
            syftTensors.map { IValue.from(it.getTorchTensor()) }.toTypedArray()

    // Generate StateOuterClass.State object using Placeholders list and syftTensor list
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
        // Generate State object from StateOuterClass.State object
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