package org.openmined.syft.datasource

import org.openmined.syft.domain.LocalConfiguration
import org.openmined.syft.networking.MessageProcessor
import org.pytorch.Module
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ModuleDataSource constructor(
    private val localConfiguration: LocalConfiguration
) {
    fun loadModule(modelName: String): Module {
        val downloadedModel =
                File("${localConfiguration.downloadPath}${File.separator}${modelName}")
        val byteArray = FileInputStream(downloadedModel).readBytes()
        val module = MessageProcessor().processTorchScript(byteArray)
        val path = saveScript(module.obj, modelName)
        return Module.load(path)
    }

    private fun saveScript(obj: com.google.protobuf.ByteString, modelName: String): String {
        val file = File(localConfiguration.modelLocation, modelName)
        FileOutputStream(file).use {
            it.write(obj.toByteArray())
            it.flush()
            it.close()
        }
        return file.absolutePath
    }
}