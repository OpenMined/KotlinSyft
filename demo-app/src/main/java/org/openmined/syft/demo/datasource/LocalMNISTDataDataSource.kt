package org.openmined.syft.demo.datasource

import android.content.res.Resources
import org.openmined.syft.demo.R
import org.openmined.syft.demo.domain.Batch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.InvalidKeyException

private const val FEATURESIZE = 784

class LocalMNISTDataDataSource constructor(
    private val resources: Resources
) {
    private var trainDataReader = returnNewReader()
    private val oneHotMap = HashMap<Int, List<Float>>()

    init {
        (0..9).forEach { i ->
            oneHotMap[i] = List(10) { idx ->
                if (idx == i)
                    1.0f
                else
                    0.0f
            }
        }
    }

    fun loadDataBatch(batchSize: Int): Pair<Batch, Batch> {
        val trainInput = arrayListOf<List<Float>>()
        val labels = arrayListOf<List<Float>>()
        for (idx in 0..batchSize) {
            val sample: List<String>? = trainDataReader.readLine()?.split(',')
            sample?.let { sampleList ->
                    trainInput.add(
                        sampleList.slice(1..FEATURESIZE).map { it.trim().toFloat() }
                    )
                oneHotMap[sampleList[0].toInt()]?.let {
                    labels.add(it)
                } ?: throw InvalidKeyException("key not found ${sampleList[0]}")
            } ?: break
        }

        //restart buffered reader to start of file
        if (labels.size == 0) {
            trainDataReader.close()
            trainDataReader = returnNewReader()
            return loadDataBatch(batchSize)
        }
        val trainingData = Batch(
            trainInput.flatten().toFloatArray(),
            longArrayOf(trainInput.size.toLong(), FEATURESIZE.toLong())
        )
        val trainingLabel = Batch(
            labels.flatten().toFloatArray(),
            longArrayOf(labels.size.toLong(), 10)
        )
        return Pair(trainingData, trainingLabel)
    }

    private fun returnNewReader() = BufferedReader(
        InputStreamReader(
            resources.openRawResource(R.raw.mnist_train)
        )
    )
}
