package org.openmined.syft.execution

import android.content.Context
import android.util.Log
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.Placeholder
import org.openmined.syft.proto.SyftTensor
import org.openmined.syftproto.execution.v1.StateOuterClass
import org.openmined.syftproto.execution.v1.StateTensorOuterClass
import org.openmined.syftproto.types.torch.v1.ScriptModuleOuterClass
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream

private const val TAG = "syft.processes.Plan"

@ExperimentalUnsignedTypes
class Plan(val planId: String, val clientConfig: ClientConfig) {
    private var planFileLocation: String? = null
    private var torchscriptLocation: String? = null
    private var planState: State? = null
    private var pytorchModule: Module? = null

    fun execute(trainingSet: Pair<IValue, IValue>) {
        val localModuleState = pytorchModule
        if (localModuleState == null) {
            Log.e(TAG, "pytorch module not initialized yet")
            return
        }
        val params = planState?.getIValueTensorArray()
        if (params == null) {
            Log.e(TAG, "parameters not deserialized yet")
            return
        }
        val x = trainingSet.first
        val y = trainingSet.second

        val batchSize = IValue.from(
            Tensor.fromBlob(longArrayOf(clientConfig.batchSize), longArrayOf(1))
        )
        val lr = IValue.from(
            Tensor.fromBlob(floatArrayOf(clientConfig.lr), longArrayOf(1))
        )
        val outputArray = localModuleState.forward(x, y, batchSize, lr, *params).toTuple()
        val beginIndex = outputArray.size-params.size
        val updatedParams = outputArray.slice(beginIndex..(beginIndex + params.size))
        
    }

    fun generateScriptModule(context: Context) {
        planFileLocation?.let {
            val scriptModule = ScriptModuleOuterClass.ScriptModule.parseFrom(
                File(it).readBytes()
            )
            torchscriptLocation = saveScript(context, scriptModule.obj)
            Log.d(TAG, "TorchScript saved at $torchscriptLocation")
            pytorchModule = Module.load(torchscriptLocation)
        } ?: Log.e(TAG, "plan file not generated yet")
    }

    private fun saveScript(context: Context, obj: com.google.protobuf.ByteString): String {
        val file = File(context.filesDir, "torchscript_${planId}.pt")
        FileOutputStream(file).use {
            it.write(obj.toByteArray())
            it.flush()
            it.close()
        }
        return file.absolutePath
    }

    data class State(
        val placeholders: List<Placeholder>,
        val syftTensors: List<SyftTensor>
    ) {
        fun getTorchTensors() = syftTensors.map { it.getTorchTensor() }
        fun getIValueTensorArray() =
                syftTensors.map { IValue.from(it.getTorchTensor()) }.toTypedArray()

        fun serialize() {
            StateOuterClass.State.newBuilder().addAllPlaceholders(
                placeholders.map { it.serialize() }
            ).addAllTensors(syftTensors.map {
                StateTensorOuterClass.StateTensor
                        .newBuilder()
                        .setTorchTensor(it.serialize())
                        .build()
            })
        }

        companion object {
            fun deserialize(state: StateOuterClass.State): State {
                val placeholders = state.placeholdersList.map {
                    Placeholder.deserialize(it)
                }
                val syftTensors = state.tensorsList.map {
                    SyftTensor.deserialize(it.torchTensor)
                }
                return State(placeholders, syftTensors)
            }
        }
    }
}