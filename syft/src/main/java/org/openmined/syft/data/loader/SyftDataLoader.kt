package org.openmined.syft.data.loader

import org.openmined.syft.data.Dataset
import org.openmined.syft.data.samplers.BatchSampler
import org.openmined.syft.data.samplers.Sampler
import org.openmined.syft.data.samplers.SequentialSampler
import org.pytorch.IValue
import org.pytorch.Tensor

/**
 * Data loader. Combines a dataset and a sampler, and provides an iterable over
 * the given dataset. It supports map-style datasets with single-process loading
 * and customizing loading order.
 * @param dataset (Dataset)from which to load the data.
 * @param batchSize (Int, optional): how many samples per batch to load (default: ``1``).
 * @param sampler (Boolean, optional) inject a sampler for the dataset
 * @param dropLast (Boolean, optional): set to ``True`` to drop the last incomplete batch,
 *                  if the dataset size is not divisible by the batch size. If ``False`` and
 *                  the size of dataset is not divisible by the batch size, then the last batch
 *                  will be smaller. (default: ``False``)
 */
class SyftDataLoader(
    var dataset: Dataset,
    var sampler: Sampler = SequentialSampler(dataset),
    var batchSize: Int = 1,
    var dropLast: Boolean = false
) : AbstractDataLoader(
    dataset, sampler, batchSize, dropLast
) {


    protected val dataLoaderIterator =
            DataLoaderIterator(this)

    override fun iterator(): Iterator<List<IValue>> = dataLoaderIterator

    override fun fetch(indices: List<Int>): List<IValue> {
        val data = arrayListOf<List<Float>>()
        val labels = arrayListOf<List<Float>>()

        val batch = arrayListOf<IValue>()

        var values: List<IValue> = emptyList()
        indices.forEach { index ->
            values = dataset.getItem(index)
            data.add(values[0].toTensor().dataAsFloatArray.toList())
            labels.add(values[1].toTensor().dataAsFloatArray.toList())
        }

        val batchSize = indices.size.toLong()
        val xFeatureLength = values[0].toTensor().shape().last()
        val yFeatureLength = values[1].toTensor().shape().last()

        batch.add(
            IValue.from(
                Tensor.fromBlob(
                    data.flatten().toFloatArray(),
                    longArrayOf(batchSize, xFeatureLength)
                )
            )
        )
        batch.add(
            IValue.from(
                Tensor.fromBlob(
                    labels.flatten().toFloatArray(),
                    longArrayOf(batchSize, yFeatureLength)
                )
            )
        )

        return batch
    }


    override fun reset() {
        indexSampler.reset()
        dataLoaderIterator.reset()
    }

}

abstract class AbstractDataLoader(
    dataset: Dataset,
    sampler: Sampler = SequentialSampler(dataset),
    batchSize: Int = 1,
    dropLast: Boolean = false
) : DataLoader {

    val indexSampler = BatchSampler(
        sampler,
        batchSize,
        dropLast
    )

    abstract fun fetch(indices: List<Int>): List<IValue>

}
