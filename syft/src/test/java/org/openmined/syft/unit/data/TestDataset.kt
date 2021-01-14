package org.openmined.syft.unit.data

import org.openmined.syft.data.Dataset
import org.pytorch.IValue
import org.pytorch.Tensor


class TestDataset : Dataset {

    private val data = arrayListOf(
        floatArrayOf(1f ,1f),
        floatArrayOf(1f ,0f),
        floatArrayOf(0f ,1f),
        floatArrayOf(0f ,0f)
    )

    private val labels = arrayListOf(
        floatArrayOf(1f),
        floatArrayOf(1f),
        floatArrayOf(1f),
        floatArrayOf(1f)
    )

    override fun getItem(index: Int): List<IValue> {
        return listOf(
            IValue.from(Tensor.fromBlob(data[index], longArrayOf(1, 2))),
            IValue.from(Tensor.fromBlob(labels[index], longArrayOf(1, 1)))
        )
    }

    override fun length(): Int = data.size

}
