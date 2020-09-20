package org.openmined.syft.datasource

import java.util.stream.IntStream.range
import org.pytorch.Tensor

/**
 * Samples elements randomly. If without replacement, then sample from a shuffled dataset.
 * If with replacement, then user can specify :attr:`num_samples` to draw.
 * @param dataSource (Dataset): dataset to sample from
 * @param numSamples (Int): number of samples to draw, default=`len(dataset)`. This argument
 *                          is supposed to be specified only when `replacement` is ``True``.
 */
class RandomSampler(val dataSource: Dataset, val numSamples: Int) :  Dataset, Sampler {

      @JvmName("getNumSamples1")
      fun getNumSamples() : Int{
        numSamples?.let {
            return numSamples
        } ?:
               return dataSource.len()
    }

    override fun getitem(index: Float) {
        super<Sampler>.getitem(index)
    }

    override fun iter() {
        super.iter()
    }

    override fun len(): Int {
        return super<Sampler>.len()
    }

}