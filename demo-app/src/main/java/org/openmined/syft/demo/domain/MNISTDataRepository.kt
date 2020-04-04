package org.openmined.syft.demo.domain

import org.openmined.syft.demo.datasource.LocalMNISTDataDataSource
import org.pytorch.IValue
import org.pytorch.Tensor
import kotlin.math.sign

class MNISTDataRepository constructor(
    private val localMNISTDataDataSource: LocalMNISTDataDataSource
) {
    fun loadData(): Pair<IValue, IValue> {
        val data = localMNISTDataDataSource.loadData()
        val tensorsX = data.first.map {
            Tensor.fromBlob(it, longArrayOf(it.size.toLong()))
        }.toTypedArray()
        val tensorsY = Tensor.fromBlob(data.second.toFloatArray(), longArrayOf(data.second.size.toLong()))

        val x = IValue.listFrom(*tensorsX)
        val y = IValue.listFrom(tensorsY)
        return Pair(x, y)
    }
}