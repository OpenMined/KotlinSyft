package org.openmined.syft.demo.federated.datasource

import android.content.res.Resources
import org.openmined.syft.demo.R
import org.openmined.syft.demo.federated.domain.Batch
import java.io.BufferedReader
import java.io.InputStreamReader

private const val FEATURESIZE = 784

class LocalMNISTDataDataSource constructor(
    private val resources: Resources
) {
    private var trainDataReader = returnDataReader()
    private var labelDataReader = returnLabelReader()
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
        val sample = readLine()

        trainInput.add(
            sample.first.map { it.trim().toFloat() }
        )
        labels.add(
            sample.second.map { it.trim().toFloat() }
        )
    }

    private fun readLine(): Pair<List<String>, List<String>> {
        var x = trainDataReader.readLine()?.split(",")
        var y = labelDataReader.readLine()?.split(",")
        if (x == null || y == null) {
            restartReader()
            x = trainDataReader.readLine()?.split(",")
            y = labelDataReader.readLine()?.split(",")
        }
        if (x == null || y == null)
            throw Exception("cannot read from dataset file")
        return Pair(x, y)
    }

    private fun restartReader() {
        trainDataReader.close()
        labelDataReader.close()
        trainDataReader = returnDataReader()
        labelDataReader = returnLabelReader()
    }

    private fun returnDataReader() = BufferedReader(
        InputStreamReader(
            resources.openRawResource(R.raw.pixels)
        )
    )

    private fun returnLabelReader() = BufferedReader(
        InputStreamReader(
            resources.openRawResource(R.raw.labels)
        )
    )
}
