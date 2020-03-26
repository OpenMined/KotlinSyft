package org.openmined.syft.demo.datasource

import android.util.Log
import org.openmined.syft.demo.R
import org.openmined.syft.networking.clients.MessageProcessor
import org.pytorch.Module
import java.io.File
import java.io.FileOutputStream

class LocalMNISTModuleDataSource constructor(
    private val resources: android.content.res.Resources,
    private val filesDir: File
) {

    fun loadModule(): Module {
        val module =
                MessageProcessor().processTorchScript(
                    resources.openRawResource(R.raw.tp_ts).readBytes()
                )
        val path = saveScript(module.obj)
        Log.d("MainActivity", "TorchScript saved at $path")
        return Module.load(path)
    }

    fun saveScript(obj: com.google.protobuf.ByteString): String {
        val file = File(filesDir, "script")
        FileOutputStream(file).use {
            it.write(obj.toByteArray())
            it.flush()
            it.close()
        }
        return file.absolutePath
    }
}