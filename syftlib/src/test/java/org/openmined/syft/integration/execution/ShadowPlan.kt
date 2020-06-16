package org.openmined.syft.integration.execution

import org.openmined.syft.execution.Plan
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import org.openmined.syft.utilities.FileWriter
import org.openmined.syftproto.execution.v1.PlanOuterClass
import org.pytorch.IValue
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.io.File

@ExperimentalUnsignedTypes
@Implements(Plan::class)
class ShadowPlan {

    @Implementation
    fun execute(
        model: SyftModel,
        trainingBatch: Pair<IValue, IValue>,
        clientConfig: ClientConfig
    ): IValue? {
        return null
    }

    @Implementation
    fun generateScriptModule(filesDir: String, torchScriptPlan: String) {
        val scriptModule = PlanOuterClass.Plan.parseFrom(
            File(torchScriptPlan).readBytes()
        )
        val torchscriptLocation = saveScript(filesDir, scriptModule.torchscript)
    }

    private fun saveScript(filesDir: String, obj: com.google.protobuf.ByteString): String {
        val fileWriter = FileWriter(filesDir, "torchscript.pt")
        fileWriter.outputStream().use {
            it.write(obj.toByteArray())
        }
        return fileWriter.absolutePath
    }
}