package org.openmined.syft.data.fetchers

import org.openmined.syft.data.Dataset
import org.pytorch.IValue

class MapDatasetFetcher(dataset: Dataset, dropLast: Boolean) :
    BaseDatasetFetcher(dataset, dropLast) {

    override fun fetch(indices: List<Int>): Pair<IValue, IValue> {
        val data = arrayListOf<FloatArray>()
        val label = arrayListOf<FloatArray>()

        indices.forEach {

        }


        return Pair(IValue.from(0), IValue.from(0))
    }

}