package org.openmined.syft.datasource

import android.util.Log
import androidx.annotation.VisibleForTesting
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syftproto.execution.v1.PlanOuterClass
import java.io.File
import java.io.InputStream

internal const val DIFF_SCRIPT_NAME = "diff_script.pt"
private const val TAG = "JobLocalDataSource"

@ExperimentalUnsignedTypes
internal class JobLocalDataSource(private val config: SyftConfiguration) {

    fun getDiffScript() =
            config.context.assets.open("torchscripts/$DIFF_SCRIPT_NAME")

    /**
     * Persist the given inputStream in the specified destination
     *
     * @return The absolute path where the file has been saved if successful.
     */
    fun save(
        input: InputStream,
        parentDir: String,
        fileName: String,
        overwrite: Boolean = true
    ): String {
        if (!File(parentDir).mkdirs())
            Log.d(TAG, "directory already exists")

        return save(input, File(parentDir, fileName), overwrite)
    }

    @VisibleForTesting
    internal fun save(input: InputStream, file: File, overwrite: Boolean): String {
        return if (file.exists() and !overwrite)
            file.absolutePath
        else {
            file.apply {
                input.use { inputStream ->
                    this.outputStream().use { outputFile ->
                        inputStream.copyTo(outputFile)
                    }
                    Log.d(TAG, "file written at ${this.absolutePath}")
                }
            }
            file.absolutePath
        }
    }
    
    /**
     * Persist the given inputStream in the specified destination asynchronously
     *
     * @return The absolute path where the file has been saved if successful.
     */
    suspend fun saveAsync(
        input: InputStream,
        parentDir: String,
        fileName: String,
        overwrite: Boolean = true
    ): String {
        if (!File(parentDir).mkdirs())
            Log.d(TAG, "directory already exists")

        return saveAsync(input, File(parentDir, fileName), overwrite)
    }

    @VisibleForTesting
    internal fun saveAsync(
        input: InputStream,
        file: File,
        overwrite: Boolean
    ): String {
        return if (file.exists() and !overwrite)
            file.absolutePath
        else {
            file.apply {
                input.use { inputStream ->
                    this.outputStream().use { outputFile ->
                        inputStream.copyTo(outputFile)
                    }
                    Log.d(TAG, "file written at ${this.absolutePath}")
                }
            }.absolutePath
        }
    }

    /**
     * Writes the module to the torchscript and returns the absolute path.
     *
     * @param parentDir The file representing the output location for the torchscript.
     * @param torchScriptPlanPath Location of the TorchScript plan
     * @param fileName The name for this file
     * @return the absolute path of the file containing the TorchScript model.
     */
    fun saveTorchScript(parentDir: String, torchScriptPlanPath: String, fileName: String): String {
        val parent = File(parentDir)
        if (!parent.exists()) parent.mkdirs()
        val file = File(parent, fileName)

        val scriptModule = PlanOuterClass.Plan.parseFrom(
            File(torchScriptPlanPath).readBytes()
        )
        return saveTorchScript(file, scriptModule)
    }

    @VisibleForTesting
    internal fun saveTorchScript(file: File, plan: PlanOuterClass.Plan): String {
        file.outputStream().use {
            it.write(plan.torchscript.toByteArray())
        }
        return file.absolutePath
    }

    @ExperimentalUnsignedTypes
    fun getModelsPath(jobId: String) = "${config.filesDir}/$jobId/models"

    @ExperimentalUnsignedTypes
    fun getPlansPath(jobId: String) = "${config.filesDir}/$jobId/plans"

    @ExperimentalUnsignedTypes
    fun getProtocolsPath(jobId: String) = "${config.filesDir}/$jobId/protocols"

}
