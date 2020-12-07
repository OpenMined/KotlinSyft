package org.openmined.syft.data.fetchers

import org.openmined.syft.data.Dataset

abstract class AbstractDatasetFetcher(val dataset: Dataset, val dropLast: Boolean) {

    abstract fun fetch(indices: Sequence<Int>): Any

}