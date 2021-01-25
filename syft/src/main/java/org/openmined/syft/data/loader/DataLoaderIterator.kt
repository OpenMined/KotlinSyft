package org.openmined.syft.data.loader

import org.pytorch.IValue

class DataLoaderIterator(private val dataLoader: SyftDataLoader) : Iterator<List<IValue>> {

    private val indexSampler = dataLoader.indexSampler

    private var currentIndex = 0

    override fun next(): List<IValue> {
        val indices = indexSampler.indices
        currentIndex += indices.size
        return dataLoader.fetch(indices)
    }

    override fun hasNext(): Boolean = currentIndex < dataLoader.dataset.length()

    fun reset() {
        currentIndex = 0
    }

}