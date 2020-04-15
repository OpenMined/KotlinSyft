package org.openmined.syft.demo.domain

import org.openmined.syft.demo.datasource.LocalMNISTDataDataSource
import org.pytorch.IValue
import org.pytorch.Tensor

class MNISTDataRepository constructor(
    private val localMNISTDataDataSource: LocalMNISTDataDataSource
) {
    fun loadData(batchSize: Int): Pair<List<IValue>, List<IValue>> {
        val data = localMNISTDataDataSource.loadData(batchSize)
        val tensorsX = data.first.map {
            IValue.from(Tensor.fromBlob(it.flattenedArray, it.shape))
        }
        val tensorsY = data.second.map {
            IValue.from(Tensor.fromBlob(it.flattenedArray, it.shape))
        }

        return Pair(tensorsX, tensorsY)
    }
}