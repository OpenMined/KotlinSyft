package org.openmined.syft.datasource

import org.openmined.syft.datasource.Sampler
import org.openmined.syft.datasource.RandomSampler
import org.openmined.syft.datasource.SequentialSampler
import org.openmined.syft.datasource.Dataset

/**
 * Data loader. Combines a dataset and a sampler, and provides an iterable over
 * the given dataset. It supports map-style datasets with single-process loading
 * and customizing loading order.
 * @param dataset (Dataset)from which to load the data.
 * @param batchSize (Int, optional): how many samples per batch to load (default: ``1``).
 * @param sampler (Sampler or Iterable, optional): defines the strategy to draw
 *                  samples from the dataset. Can be any ``Iterable`` with ``__len__``
 *                  implemented. If specified, :attr:`shuffle` must not be specified.
 * @param dropLast (Boolean, optional): set to ``True`` to drop the last incomplete batch,
 *                  if the dataset size is not divisible by the batch size. If ``False`` and
 *                  the size of dataset is not divisible by the batch size, then the last batch
 *                  will be smaller. (default: ``False``)
 */
class DataLoader(var dataset: Dataset,
                 var batchsize: Int = 1,
                 var sampler: Sampler,
                 var dropLast: Boolean = false
                ) : Dataset, Sampler {

    override fun len(): Int {
        return this.sampler.len()
    }

}