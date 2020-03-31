package org.openmined.syft.demo.domain

import org.openmined.syft.demo.datasource.LocalMNISTDataDataSource
import org.pytorch.IValue
import org.pytorch.Tensor

class MNISTDataRepository constructor(
    private val localMNISTDataDataSource: LocalMNISTDataDataSource
) {
    fun loadData(): Pair<IValue, IValue> {
        val data = localMNISTDataDataSource.loadData()
        val x = IValue.listFrom(Tensor.fromBlob(data.first[0], longArrayOf(784)))
        val y = IValue.listFrom(Tensor.fromBlob(data.first[0], longArrayOf(784)))
        return Pair(x, y)
    }
}