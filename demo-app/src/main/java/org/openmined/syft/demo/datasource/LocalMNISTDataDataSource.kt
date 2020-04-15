package org.openmined.syft.demo.datasource

import android.content.res.Resources
import org.openmined.syft.demo.R
import java.io.BufferedReader
import java.io.InputStreamReader

class LocalMNISTDataDataSource constructor(
    private val resources: Resources
) {
    fun loadData(batchSize: Int): Pair<List<Batch>, List<Batch>> {
        val trainInput = arrayListOf<List<Float>>()
        val labels = arrayListOf<Float>()

        val x = resources.openRawResource(R.raw.train_small)
        BufferedReader(InputStreamReader(x))
                .forEachLine { line ->
                    trainInput.add(
                        line.split(',')
                                .map {
                                    it.trim().toFloat() / 255
                                }
                    )
                }

        val y = resources.openRawResource(R.raw.labels_small)
        BufferedReader(InputStreamReader(y))
                .forEachLine { line ->
                    labels.add(line.toFloat() / 10)
                }
        val trainingData = trainInput.chunked(batchSize) { batch: List<List<Float>> ->
            Batch(
                batch.flatten().toFloatArray(),
                longArrayOf(batch.size.toLong(), 784)
            )
        }
        val trainingLabel = labels.chunked(batchSize) { batch: List<Float> ->
            Batch(
                batch.toFloatArray(),
                longArrayOf(batch.size.toLong(), 1)
            )
        }
        return Pair(trainingData, trainingLabel)
    }
}

data class Batch(val flattenedArray: FloatArray, val shape: LongArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Batch

        if (!flattenedArray.contentEquals(other.flattenedArray)) return false
        if (!shape.contentEquals(other.shape)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flattenedArray.contentHashCode()
        result = 31 * result + shape.contentHashCode()
        return result
    }
}