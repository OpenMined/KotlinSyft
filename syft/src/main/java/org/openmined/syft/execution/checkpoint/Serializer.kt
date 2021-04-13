package org.openmined.syft.execution.checkpoint

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject


interface Serializer<IN, OUT> {
    fun serialize(data: IN)

    fun deserialize(data: OUT): IN
}

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
class CheckPointSerializer: Serializer<CheckPoint, JSONObject> {
    override fun serialize(data: CheckPoint) {
        // TODO: serialize checkpoint
    }

    override fun deserialize(data: JSONObject): CheckPoint {
        // TODO: deserialize checkpoint
        return CheckPoint(
            steps = data.getInt("steps"),
            currentStep = data.getInt("current_step"),
            batchSize = data.getInt("batch_size")
        )
    }
}
