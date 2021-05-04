package org.openmined.syft.execution.checkpoint

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.datamodels.ClientConfigSerializer
import org.openmined.syft.networking.datamodels.ClientProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
interface CheckPointSerializer<OUT> {
    fun serialize(checkPoint: CheckPoint): OUT

    fun deserialize(data: OUT): CheckPoint

    fun save(checkPoint: CheckPoint, path: String, overwrite: Boolean = false) : String

    fun load(path: String) : CheckPoint
}

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
class JsonCheckPointSerializer: CheckPointSerializer<JSONObject> {

    private lateinit var serialized: JSONObject

    override fun serialize(checkPoint: CheckPoint): JSONObject {
        val json = JSONObject()
        json.put("steps", checkPoint.steps)
        json.put("current_step", checkPoint.currentStep)
        json.put("batch_size", checkPoint.currentStep)
        checkPoint.clientConfig?.let {
            val clientConfigJson = JSONObject()

            val properties = JSONObject()
            properties.put("model_name", it.properties.modelName)
            properties.put("model_version", it.properties.modelVersion)
            properties.put("max_updates", it.properties.maxUpdates)

            clientConfigJson.put("properties", properties)
            clientConfigJson.put("plan_args", it.planArgs)
            json.put("client_config", clientConfigJson)
        }
        serialized = json
        return json
    }

    override fun deserialize(data: JSONObject): CheckPoint {

        val checkPoint = CheckPoint(
            steps = data.getInt("steps"),
            currentStep = data.getInt("current_step"),
            batchSize = data.getInt("batch_size")
        )

        if (data.has("client_config")) {
            val configJson = data.getJSONObject("client_config")
            val propertiesJson = configJson.getJSONObject("properties")
            val args = mutableMapOf<String, String>()

            val clientConfig = ClientConfig(
                properties = ClientProperties(
                    modelName = propertiesJson.getString("model_name"),
                    maxUpdates = propertiesJson.getInt("max_updates"),
                    modelVersion = propertiesJson.getString("model_version")
                ),
                planArgs = args
            )

            checkPoint.clientConfig = clientConfig
        }

        return checkPoint
    }

    override fun save(
        checkPoint: CheckPoint,
        path: String,
        overwrite: Boolean
    ) : String {
        val file = File(path)
        return if (file.exists() and !overwrite)
            file.absolutePath
        else {
            val out = ObjectOutputStream(FileOutputStream(file))
            out.writeObject(checkPoint)
            out.close()
            file.absolutePath
        }
    }

    override fun load(path: String) : CheckPoint {
        val file = File(path)
        if (!file.exists())
            throw FileNotFoundException()

        val input = ObjectInputStream(FileInputStream(file))
        return input.readObject() as CheckPoint
    }
}
