package org.openmined.syft.data

import org.openmined.syft.data.fetchers.BaseDatasetFetcher
import org.openmined.syft.data.samplers.BatchSampler
import org.openmined.syft.data.samplers.RandomSampler
import org.openmined.syft.data.samplers.Sampler
import org.openmined.syft.data.samplers.SequentialSampler
import org.pytorch.IValue

/**
 * Data loader. Combines a dataset and a sampler, and provides an iterable over
 * the given dataset. It supports map-style datasets with single-process loading
 * and customizing loading order.
 * @param dataset (Dataset)from which to load the data.
 * @param batchSize (Int, optional): how many samples per batch to load (default: ``1``).
 * @param shuffle (Boolean, optional) to define the sampler for the dataset
 * @param dropLast (Boolean, optional): set to ``True`` to drop the last incomplete batch,
 *                  if the dataset size is not divisible by the batch size. If ``False`` and
 *                  the size of dataset is not divisible by the batch size, then the last batch
 *                  will be smaller. (default: ``False``)
 */
class DataLoader(var dataset: Dataset,
                 var batchSize: Int = 1,
                 var shuffle: Boolean = false,
                 var dropLast: Boolean = false
) : Iterable<Pair<IValue, IValue>> {

    private var sampler: Sampler = if (shuffle) {
        RandomSampler(dataset)
    } else {
        SequentialSampler(dataset)
    }

    private val batchSampler = BatchSampler(
        sampler,
        batchSize,
        dropLast
    )

    fun indexSampler() = batchSampler

    override fun iterator(): Iterator<Pair<IValue, IValue>> = BaseDataLoaderIterator(this)
}

open class BaseDataLoaderIterator(dataLoader: DataLoader) : Iterator<Pair<IValue, IValue>> {

    private val indexSampler = dataLoader.indexSampler()

    private val datasetFetcher = BaseDatasetFetcher(dataLoader.dataset)

    private val datasetLength = datasetFetcher.dataset.length()

    private var currentIndex = 0

    override fun next(): Pair<IValue, IValue> {
        val indices = indexSampler.indices()
        currentIndex += indices.size
        return datasetFetcher.fetch(indices)
    }

    override fun hasNext(): Boolean {
        return currentIndex < datasetLength
    }
}




