package org.openmined.syft.execution

import android.util.Log
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.PytorchTensorWrapper
import org.openmined.syft.proto.SyftModel
import org.openmined.syftproto.types.torch.v1.ScriptModuleOuterClass
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream

private const val TAG = "syft.processes.Plan"

@ExperimentalUnsignedTypes
class Plan(val planId: String) {
    private var pytorchModule: Module? = null

    @ExperimentalStdlibApi
    fun execute(
        model: SyftModel,
        trainingBatch: Pair<IValue, IValue>,
        clientConfig: ClientConfig
    ): List<Any?> {
        val localModuleState = pytorchModule
        if (localModuleState == null) {
            Log.e(TAG, "pytorch module not initialized yet")
            return listOf()
        }
        val params = model.modelState?.getIValueTensorArray()
        if (params == null) {
            Log.e(TAG, "model state not initialised yet")
            return listOf()
        }
        val x = trainingBatch.first
        val y = trainingBatch.second

        val batchSize = IValue.from(
            Tensor.fromBlob(longArrayOf(clientConfig.batchSize), longArrayOf(1))
        )
        val lr = IValue.from(
            Tensor.fromBlob(floatArrayOf(clientConfig.lr), longArrayOf(1))
        )
        val outputArray = localModuleState.forward(x, y, batchSize, lr, *params).toTuple()
        val beginIndex = outputArray.size-params.size
        val updatedParams = outputArray.slice(beginIndex..(beginIndex + params.size))
        val diff = model.updateAndCreateDiff(
            updatedParams.map {
                PytorchTensorWrapper(it.toTensor())
            }
        )
        return listOf(outputArray.slice(0..beginIndex), diff)
    }

    fun generateScriptModule(filesDir: String, torchScriptPlan: String) {
        val scriptModule = ScriptModuleOuterClass.ScriptModule.parseFrom(
            File(torchScriptPlan).readBytes()
        )
        val torchscriptLocation = saveScript(filesDir, scriptModule.obj)
        Log.d(TAG, "TorchScript saved at $torchscriptLocation")
        pytorchModule = Module.load(torchscriptLocation)
    }

    private fun saveScript(filesDir: String, obj: com.google.protobuf.ByteString): String {
        val file = File(filesDir, "torchscript_${planId}.pt")
        FileOutputStream(file).use {
            it.write(obj.toByteArray())
            it.flush()
            it.close()
        }
        return file.absolutePath
    }
}