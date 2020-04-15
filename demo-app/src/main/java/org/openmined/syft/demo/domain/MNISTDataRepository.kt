package org.openmined.syft.demo.domain

import org.openmined.syft.demo.datasource.LocalMNISTDataDataSource
import org.pytorch.IValue
import org.pytorch.Tensor

class MNISTDataRepository constructor(
    private val localMNISTDataDataSource: LocalMNISTDataDataSource
) {
    fun loadData(): Pair<List<IValue>, List<IValue>> {
        val data = localMNISTDataDataSource.loadData()
        val tensorsX = data.first.map {
            IValue.from(Tensor.fromBlob(it, longArrayOf(1L, 784L)))
        }
        val tensorsY = data.second.map {
            IValue.from(Tensor.fromBlob(floatArrayOf(it), longArrayOf(1, 1)))
        }

        return Pair(tensorsX, tensorsY)
    }
}