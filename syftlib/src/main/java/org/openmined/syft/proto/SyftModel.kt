package org.openmined.syft.proto

import android.util.Log
import org.openmined.syftproto.execution.v1.StateOuterClass
import org.pytorch.Tensor
import java.io.File

private const val TAG = "SyftModel"

/**
 * SyftModel is the data model class for storing the weights of the neural network used for
 * training or inference.
 *
 * @author Varun Khare
 * @since 2020-03-29
 */
@ExperimentalUnsignedTypes
data class SyftModel(
    /**
     * modelName: A string to hold the name of the model specified while hosting the plan on pygrid.
     */
    val modelName: String,

    /**
     * version: A string specifying the version of the model.
     */
    val version: String? = null,

    /**
     * pyGridModelId: A unique id assigned by Pygrid to very model hosted over it.
     * pyGridModelId is used for downloading the appropriate model files from Pygrid as an argument.
     */
    var pyGridModelId: String? = null,

    /**
     * modelState: Responsible for Holding the model weights of the neural network.
     */
    var modelState: State? = null
) {
//todo change this according to optimal way for sending diff
//    fun updateAndCreateDiff(newModelParams: List<PytorchTensorWrapper>): State? {
//        val diff = mutableListOf<SyftTensor>()
//        modelState?.let { currentState ->
//            newModelParams.forEachIndexed { index, value ->
//                diff[index] = SyftTensor.fromTorchTensor(
//                    currentState.syftTensors[index].getTorchTensorWrapper() - value
//                )
//                currentState.syftTensors[index] = SyftTensor.fromTorchTensor(value)
//            }
//            return currentState.copy(syftTensors = diff)
//        } ?: return null
//    }

    /**
     * This method is used to save/update SyftModel class.
     * This function must be called after every gradient step to update the model state for
     * further plan executions.
     * It throws an IllegalArgumentException if the size newModelParams is not correct.
     *
     * @param newModelParams a list of pytorch Tensor that would be converted to syftTensor
     * @sample model.updateModel(updatedParams.map { it.toTensor() })
     */
    fun updateModel(newModelParams: List<Tensor>) {
        modelState?.let { state ->
            if (state.syftTensors.size != newModelParams.size) {
                throw IllegalArgumentException("The size of the list of new parameters ${newModelParams.size} is different than the list of params of the model ${state.syftTensors.size}")
            }
            newModelParams.forEachIndexed { index, pytorchTensor ->
                state.syftTensors[index] = pytorchTensor.toSyftTensor()
            }
        }
    }

    /**
     * This method is used to load SyftModel from file
     *
     * @param modelFile the filepath containing model state.
     * @sample model.loadModelState(modelFile)
     */
    fun loadModelState(modelFile: String) {
        modelState = State.deserialize(
            StateOuterClass.State.parseFrom(File(modelFile).readBytes())
        )
        Log.d(TAG, "Model loaded from $modelFile")
    }
}

