package org.openmined.syft.datasource

import android.util.Log
import androidx.annotation.VisibleForTesting
import io.reactivex.Single
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
}
