package org.openmined.syft.datasource

import org.openmined.syft.domain.LocalConfiguration
import org.openmined.syft.networking.MessageProcessor
import org.pytorch.Module
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PlanDataSource constructor(
    private val localConfiguration: LocalConfiguration
) {
    fun loadPlan(planPath: String): Module {
        val downloadedModel = File("$planPath.pb")
        val byteArray = FileInputStream(downloadedModel).readBytes()
        val plan = MessageProcessor().processTorchScript(byteArray)
        val path = saveScript(plan.obj, planPath)
        return Module.load(path)
    }

    private fun saveScript(obj: com.google.protobuf.ByteString, planPath: String): String {
        val file = File(planPath)
        FileOutputStream(file).use {
            it.write(obj.toByteArray())
            it.flush()
            it.close()
        }
        return file.absolutePath
    }
}