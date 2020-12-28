package org.openmined.syft.domain

import org.pytorch.IValue

interface SyftDataLoader {
    fun loadDataBatch(batchSize: Int): Pair<IValue, IValue>
}
