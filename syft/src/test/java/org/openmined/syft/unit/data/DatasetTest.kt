package org.openmined.syft.unit.data

import org.openmined.syft.data.Dataset
import org.openmined.syft.data.KTensor


class DatasetTest : Dataset {

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

    override fun getItem(index: Int): Pair<KTensor, KTensor> {
        return Pair(
            KTensor(data[index], longArrayOf(1, 2)),
            KTensor(labels[index], longArrayOf(1, 1))
        )
    }

    override fun length(): Int = data.size

}