package org.openmined.syft.demo.federated.datasource

import android.content.res.Resources
import org.openmined.syft.data.Dataset
import org.openmined.syft.demo.R
import org.pytorch.IValue
import org.pytorch.Tensor
import java.io.BufferedReader
import java.io.InputStreamReader

private const val FEATURESIZE = 784
private const val DATASET_LENGTH = 1000

class MNISTDataset(private val resources: Resources) : Dataset {

    private var trainDataReader = returnDataReader()
    private var labelDataReader = returnLabelReader()
    private val oneHotMap = HashMap<Int, List<Float>>()

    private val trainInput = arrayListOf<List<Float>>()
    private val labels = arrayListOf<List<Float>>()

    init {
        (0..9).forEach { i ->
            oneHotMap[i] = List(10) { idx ->
                if (idx == i)
                    1.0f
                else
                    0.0f
            }
        }

        readAllData()
    }

    override val length: Int = trainInput.size

    override fun getItem(index: Int): List<IValue> {
        val trainingData = IValue.from(
            Tensor.fromBlob(
                trainInput[index].toFloatArray(),
                longArrayOf(1, FEATURESIZE.toLong())
            )
        )

        val trainingLabel = IValue.from(
            Tensor.fromBlob(
                labels[index].toFloatArray(),
                longArrayOf(1, 10)
            )
        )

        return listOf(trainingData, trainingLabel)
    }

    private fun readAllData() {
        for (i in 0 until DATASET_LENGTH)
            readSample(trainInput, labels)
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