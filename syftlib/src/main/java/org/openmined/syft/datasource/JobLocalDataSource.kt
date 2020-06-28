package org.openmined.syft.datasource

import android.util.Log
import androidx.annotation.VisibleForTesting
import io.reactivex.Single
import org.openmined.syftproto.execution.v1.PlanOuterClass
import java.io.File
import java.io.InputStream

private const val TAG = "JobLocalDataSource"

internal class JobLocalDataSource {

    /**
     * Persist the given inputStream in the specified destination.
     *
     * @return The absolute path where the file has been saved if successful.
     */
    fun save(input: InputStream, parentDir: String, fileName: String): Single<String> {
        if (!File(parentDir).mkdirs())
            Log.d(TAG, "directory already exists")

        return save(input, File(parentDir, fileName))
    }

    @VisibleForTesting
    internal fun save(input: InputStream, file: File): Single<String> {
        file.apply {
            return Single.create { emitter ->
                input.use { inputStream ->
                    this.outputStream().use { outputFile ->
                        inputStream.copyTo(outputFile)
                    }
                    Log.d(TAG, "file written at ${this.absolutePath}")
                    emitter.onSuccess(this.absolutePath)
                }
            }
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
}
