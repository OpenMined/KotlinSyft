package org.openmined.syft.proto

import android.util.Log
import org.openmined.syftproto.execution.v1.StateOuterClass
import org.pytorch.Tensor
import java.io.File

private const val TAG = "SyftModel"

@ExperimentalUnsignedTypes
data class SyftModel(
    val modelName: String,
    val version: String? = null,
    var pyGridModelId: String? = null,
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

    fun updateModel(newModelparam: List<Tensor>) {
        modelState?.let { state ->
            newModelparam.forEachIndexed { index, pytorchTensor ->
                state.syftTensors[index] = SyftTensor.fromTorchTensor(pytorchTensor)
            }
        }
    }
    fun loadModelState(modelFile: String) {
        modelState = State.deserialize(
            StateOuterClass.State.parseFrom(File(modelFile).readBytes())
        )
        Log.d(TAG, "Model loaded from $modelFile")
    }
}

