package org.openmined.syft.proto

import android.util.Log
import org.pytorch.Tensor

private const val TAG = "SyftModel"

/**
 * SyftModel is the data model class for storing the weights of the neural network used for
 * training or inference.
 * @property modelName: A string to hold the name of the model specified while hosting the plan on pygrid.
 * @property version: A string specifying the version of the model.
 * @property pyGridModelId: A unique id assigned by Pygrid to very model hosted over it. pyGridModelId is used for downloading the appropriate model files from PyGrid as an argument.
 * @property modelSyftState: Responsible for Holding the model weights of the neural network.
 */
@ExperimentalUnsignedTypes
data class SyftModel(
    val modelName: String,
    val version: String? = null,
    var pyGridModelId: String? = null,
    var modelSyftState: SyftState? = null
) {

    /**
     * Subtract the older state from the current state to generate the diff for Upload to PyGrid
     * @see SyftState.createDiff
     * @param oldSyftState The state with respect to which the diff will be generated
     * @param diffScriptLocation The location of the torchscript for performing the subtraction
     * @throws IllegalArgumentException if model params are not downloaded yet.
     */
    fun createDiff(oldSyftState: SyftState, diffScriptLocation: String): SyftState {
        return modelSyftState?.createDiff(oldSyftState, diffScriptLocation)
               ?: throw IllegalStateException("Model parameters not downloaded yet")
    }

    /**
     * This method is used to save/update SyftModel class.
     * This function must be called after every gradient step to update the model state for further plan executions.
     * @throws IllegalArgumentException if the size newModelParams is not correct.
     * @param newModelParams a list of pytorch Tensor that would be converted to syftTensor
     * @sample model.updateModel(updatedParams.map { it.toTensor() })
     */
    fun updateModel(newModelParams: List<Tensor>) {
        modelSyftState?.let { state ->
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
     * @param modelFile the filepath containing model state.
     *
     * example :
     * ```kotlin
     * model.loadModelState(modelFile)
     * ```
     */
    fun loadModelState(modelFile: String) {
        modelSyftState = SyftState.loadSyftState(modelFile)
        Log.d(TAG, "Model loaded from $modelFile")
    }
}

