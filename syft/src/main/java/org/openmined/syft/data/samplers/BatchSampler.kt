package org.openmined.syft.data.samplers

import kotlin.math.ceil
import kotlin.math.floor

/**
 * Wraps another sampler to yield a mini-batch of indices.
 * @property sampler (Sampler or Iterable): Base sampler. Can be any iterable object
 * @property batchSize (Int): Size of mini-batch.
 * @property dropLast (Boolean): If ``True``, the sampler will drop the last batch if
 *                          its size would be less than ``batchSize``
 */
class BatchSampler(
    private val sampler: Sampler,
    private val batchSize: Int = 1,
    private val dropLast: Boolean = false
) : Sampler {

    override fun iter(): Sequence<Int> {
        //TODO: implement batch
        return sequenceOf(1)
    }

    override fun length(): Int {
        if (dropLast) return floor(1.0 * sampler.length() / batchSize).toInt()
        return ceil(1.0 * sampler.length() / batchSize).toInt()
    }

}