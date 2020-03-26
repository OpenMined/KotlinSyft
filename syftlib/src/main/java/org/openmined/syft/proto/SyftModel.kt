package org.openmined.syft.proto

import java.util.*
import kotlin.collections.HashMap

class SyftModel {
    var params = HashMap<String, FloatArray>()
    var paramIndex = HashMap<String, Int>()

    fun createSerializedDiff(newModelParams: HashMap<String, FloatArray>) {
        val diff = HashMap<String, FloatArray>()
        newModelParams.forEach { (key, value) ->
            params[key]?.let {
                diff[key] = it - value
            }
            ?: throw InvalidPropertiesFormatException("The updated model does not match with the original model")
        }

    }
}

private operator fun FloatArray.minus(value: FloatArray): FloatArray {
    return this.zip(value).map { it.first - it.second }.toFloatArray()
}
