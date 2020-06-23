package org.openmined.syft.utilities

import android.util.Log
import io.reactivex.Single
import java.io.File
import java.io.InputStream

const val MB = 1024 * 1024
private const val TAG = "utilities.File"

class FileWriter(private val parentPath: File, fileName: String) : File(parentPath, fileName) {

    constructor(parentPath: String, fileName: String) : this(File(parentPath), fileName)

    fun writeRandomData(sizeInMB: Int): FileWriter {
        this.bufferedWriter().use { output ->
            repeat(sizeInMB) {
                output.write("x".repeat(MB))
            }
        }
        return this
    }

    fun writeInputStream(input: InputStream?): Single<String> {
        if (!this.parentPath.mkdirs())
            Log.d(TAG, "directory already exists")

        return Single.create { emitter ->
            input?.use { inputStream ->
                this.outputStream().use { outputFile ->
                    inputStream.copyTo(outputFile)
                }
                Log.d(TAG, "file written at ${this.absolutePath}")
                emitter.onSuccess(this.absolutePath)
            }
        }
    }
}

fun InputStream.readNBuffers(
    n: Int,
    bufferArray: ByteArray? = null
): Int {
    val smallBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var count = 0
    for (i in 0..(n / DEFAULT_BUFFER_SIZE)) {
        val line = this.read(smallBuffer)
        if (line == -1)
            break
        if (bufferArray != null) {
            val offset = count % bufferArray.size
            if (offset != count)
                Log.w(TAG, "Overriding buffer values due to overflow")
            val endIdx = Integer.min(line, bufferArray.size - offset)
            if (endIdx != DEFAULT_BUFFER_SIZE)
                smallBuffer.sliceArray(0..endIdx).copyInto(bufferArray, offset)
            else
                smallBuffer.copyInto(bufferArray, offset)
        }
        count += line
    }
    return count
}
