package org.openmined.syft.proto

import org.openmined.syftproto.execution.v1.StateOuterClass
import org.openmined.syftproto.execution.v1.StateTensorOuterClass
import org.pytorch.IValue
import java.io.File

/** SyftState class is responsible for storing all the weights of the neural network.
 * We update these model weights after every plan.execute
 * @property placeholders the variables describing the location of tensor in the plan torchscript
 * @property syftTensors the tensors for the model params
 * @sample SyftModel.updateModel
 * @sample SyftModel.loadModelState
 */
@ExperimentalUnsignedTypes
data class SyftState(
    val placeholders: Array<Placeholder>,
    val syftTensors: Array<SyftTensor>
) {

    companion object {
        /**
         * Load the [SyftTensors][SyftTensor] and [placeholders][Placeholder] from the file
         */
        @ExperimentalUnsignedTypes
        fun loadSyftState(fileLocation: String): SyftState {
            return StateOuterClass.State.parseFrom(File(fileLocation).readBytes()).toSyftState()
        }
    }

    /**
     * Subtract the older state from the current state to generate the diff
     * @param oldSyftState The state with respect to which the diff will be generated
     * @param diffScriptLocation The location of the torchscript for performing the subtraction
     * @throws IllegalArgumentException if the size newModelParams is not same.
     */
    fun createDiff(oldSyftState: SyftState, diffScriptLocation: String): SyftState {
        if (this.syftTensors.size != oldSyftState.syftTensors.size)
            throw IllegalArgumentException("Dimension mismatch. Original model params have size ${oldSyftState.syftTensors.size} while input size is ${this.syftTensors.size}")
        val diff = Array(size = this.syftTensors.size) { index ->
            this.syftTensors[index].applyOperation(
                diffScriptLocation,
                oldSyftState.syftTensors[index]
            )
        }
        return this.copy(syftTensors = diff)
    }

    /**
     * @return an array of pyTorch [org.pytorch.IValue] from the SyftTensors list
     */
    fun getIValueTensorArray() =
            syftTensors.map { IValue.from(it.getTorchTensor()) }.toTypedArray()

    /**
     * Generate StateOuterClass.State object using Placeholders list and syftTensor list
     */
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SyftState

        if (!placeholders.contentEquals(other.placeholders)) return false
        if (!syftTensors.contentEquals(other.syftTensors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = placeholders.contentHashCode()
        result = 31 * result + syftTensors.contentHashCode()
        return result
    }
}

/**
 * Generate State object from StateOuterClass.State object
 */
@ExperimentalUnsignedTypes
fun StateOuterClass.State.toSyftState(): SyftState {
    val placeholders = this.placeholdersList.map {
        Placeholder.deserialize(it)
    }.toTypedArray()
    val syftTensors = this.tensorsList.map {
        it.torchTensor.toSyftTensor()
    }.toTypedArray()
    return SyftState(placeholders, syftTensors)
}