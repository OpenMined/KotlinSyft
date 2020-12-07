package org.openmined.syft.data.fetchers

import org.openmined.syft.data.Dataset

class MapDatasetFetcher(dataset: Dataset, dropLast: Boolean) :
    AbstractDatasetFetcher(dataset, dropLast) {

    override fun fetch(indices: Sequence<Int>): Any {
        return listOf(Pair(0, 0))
    }

}