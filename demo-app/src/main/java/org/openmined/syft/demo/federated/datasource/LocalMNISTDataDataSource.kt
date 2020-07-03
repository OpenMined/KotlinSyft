package org.openmined.syft.demo.federated.datasource

import android.content.res.Resources
import org.openmined.syft.demo.R
import org.openmined.syft.demo.federated.domain.Batch
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
        for (idx in 0..batchSize)
            readSample(trainInput, labels)

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

    private fun readSample(
        trainInput: ArrayList<List<Float>>,
        labels: ArrayList<List<Float>>
    ) {
        val sample = trainDataReader.readLine()?.split(',') ?: run {
            restartReader()
            trainDataReader.readLine()?.split(',')
        } ?: throw Exception("cannot read from dataset file")

        trainInput.add(
            sample.slice(1..FEATURESIZE).map { it.trim().toFloat() }
        )
        oneHotMap[sample[0].toInt()]?.let {
            labels.add(it)
        } ?: throw InvalidKeyException("key not found ${sample[0]}")
    }

    private fun restartReader() {
        trainDataReader.close()
        trainDataReader = returnNewReader()
    }

    private fun returnNewReader() = BufferedReader(
        InputStreamReader(
            resources.openRawResource(R.raw.mnist_train)
        )
    )
}
