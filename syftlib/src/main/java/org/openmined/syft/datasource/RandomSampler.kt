package org.openmined.syft.datasource

import java.util.stream.IntStream.range
import org.pytorch.Tensor

/**
 * Samples elements randomly. If without replacement, then sample from a shuffled dataset.
 * If with replacement, then user can specify :attr:`num_samples` to draw.
 * @param dataSource (Dataset): dataset to sample from
 * @param replacement (Boolean): samples are drawn on-demand with replacement if ``True``, default=``False``
 * @param numSamples (Int): number of samples to draw, default=`len(dataset)`. This argument
 *                          is supposed to be specified only when `replacement` is ``True``.
 */
class RandomSampler(override var dataSource: Dataset, var replacement: Boolean = false,
                    var numSamples: Int = dataSource.len()) : Sampler(dataSource) {

     fun numsamples() : Int{

         if (numSamples == null) {
             return dataSource.len()
         }
         if (numSamples != null) {
            return numSamples
         }
         return 0
    }

    override fun iter(){}

    override fun len(): Int {
        return numSamples
    }

}