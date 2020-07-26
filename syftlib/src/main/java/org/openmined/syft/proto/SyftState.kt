package org.openmined.syft.proto

import org.openmined.syftproto.execution.v1.StateOuterClass
import org.openmined.syftproto.execution.v1.StateTensorOuterClass
import org.pytorch.IValue
import java.io.File

/** SyftState class is responsible for storing all the weights of the neural network.
 * We update these model weights after every plan.execute
 * @property placeholders the variables describing the location of tensor in the plan torchscript
 * @property iValueTensors the IValue tensors for the model params
 * @sample SyftModel.updateModel
 * @sample SyftModel.loadModelState
 */
@ExperimentalUnsignedTypes
data class SyftState(
    val placeholders: Array<Placeholder>,
    val iValueTensors: Array<IValue>
) {

    /**
     * @return an array of [SyftTensor] from the [State][[https://pytorch.org/javadoc/org/pytorch/IValue.html] IValue Array
     */
    val syftTensorArray get() = iValueTensors.map { it.toTensor().toSyftTensor() }.toTypedArray()

    /**
     * @return an array of pyTorch [Tensors][https://pytorch.org/javadoc/org/pytorch/Tensor.html] from the SyftTensors list
     */
    val tensorArray get() = iValueTensors.map { it.toTensor() }.toTypedArray()

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
     * This method is used to save/update SyftState parameters.
     * @throws IllegalArgumentException if the size newModelParams is not correct.
     * @param newStateTensors a list of PyTorch Tensor that would be converted to syftTensor
     */
    fun updateState(newStateTensors: Array<IValue>) {
        if (iValueTensors.size != newStateTensors.size) {
            throw IllegalArgumentException("The size of the list of new parameters ${newStateTensors.size} is different than the list of params of the model ${iValueTensors.size}")
        }
        newStateTensors.forEachIndexed { idx, value ->
            iValueTensors[idx] = value
        }
    }

    /**
     * Subtract the older state from the current state to generate the diff
     * @param oldSyftState The state with respect to which the diff will be generated
     * @param diffScriptLocation The location of the torchscript for performing the subtraction
     * @throws IllegalArgumentException if the size newModelParams is not same.
     */
    fun createDiff(oldSyftState: SyftState, diffScriptLocation: String): SyftState {
        if (this.iValueTensors.size != oldSyftState.iValueTensors.size)
            throw IllegalArgumentException("Dimension mismatch. Original model params have size ${oldSyftState.iValueTensors.size} while input size is ${this.iValueTensors.size}")
        val diff = Array(size = this.iValueTensors.size) { index ->
            this.iValueTensors[index].applyOperation(
                diffScriptLocation,
                oldSyftState.iValueTensors[index]
            )
        }
        val localPlaceHolders = diff.mapIndexed { idx, _ ->
            Placeholder(
                idx.toString(),
                listOf("$idx", "#state-$idx")
            )
        }.toTypedArray()
        return SyftState(placeholders = localPlaceHolders, iValueTensors = diff)
    }

    /**
     * Generate StateOuterClass.State object using Placeholders list and syftTensor list
     */
    fun serialize(): StateOuterClass.State {
        return StateOuterClass.State.newBuilder().addAllPlaceholders(
            placeholders.map { it.serialize() }
        ).addAllTensors(syftTensorArray.map {
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
        if (!iValueTensors.contentEquals(other.iValueTensors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = placeholders.contentHashCode()
        result = 31 * result + iValueTensors.contentHashCode()
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
        it.torchTensor.toSyftTensor().getIValue()
    }.toTypedArray()
    return SyftState(placeholders, syftTensors)
}