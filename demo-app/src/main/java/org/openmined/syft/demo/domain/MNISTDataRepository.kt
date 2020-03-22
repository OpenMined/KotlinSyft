package org.openmined.syft.demo.domain

import org.openmined.syft.demo.datasource.LocalMNISTDataDataSource
import java.util.ArrayList

class MNISTDataRepository constructor(
    private val localMNISTDataDataSource: LocalMNISTDataDataSource
) {
    fun loadData(): Pair<ArrayList<FloatArray>, ArrayList<Float>> {
        return localMNISTDataDataSource.loadData()
    }
}