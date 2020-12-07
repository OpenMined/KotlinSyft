package org.openmined.syft.data.samplers

import org.openmined.syft.data.Dataset

/**
 * Samples elements sequentially, always in the same order.
 * @property dataset (Dataset): dataset to sample from
 */
class SequentialSampler(private val dataset: Dataset) :
    Sampler {

    override fun iter(): Sequence<Int> = (0 until dataset.length()).asSequence()

    override fun length(): Int = dataset.length()

}