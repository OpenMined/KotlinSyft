package org.openmined.syft.demo.datasource

import android.content.res.Resources
import org.openmined.syft.demo.R
import java.io.BufferedReader
import java.io.InputStreamReader

class LocalMNISTDataDataSource constructor(
    private val resources: Resources
) {
    fun loadData(): Pair<List<FloatArray>, List<Float>> {
        val trainInput = arrayListOf<FloatArray>()
        val labels = arrayListOf<Float>()

        val x = resources.openRawResource(R.raw.train_small)
        BufferedReader(InputStreamReader(x))
                .forEachLine { line ->
                    trainInput.add(
                        line.split(',')
                                .map {
                                    it.trim().toFloat() / 255
                                }.toFloatArray()
                    )
                }

        val y = resources.openRawResource(R.raw.labels_small)
        BufferedReader(InputStreamReader(y))
                .forEachLine { line ->
                    labels.add(line.toFloat() / 10)
                }
        return Pair(trainInput, labels)
    }
}