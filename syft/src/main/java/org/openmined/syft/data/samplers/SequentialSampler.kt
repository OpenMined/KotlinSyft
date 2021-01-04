package org.openmined.syft.data.samplers

import org.openmined.syft.data.Dataset

/**
 * Samples elements sequentially, always in the same order.
 * @property dataset (Dataset): dataset to sample from
 */
class SequentialSampler(private val dataset: Dataset) :
    Sampler {

    override fun indices() = List(dataset.length()) { it }

    override fun length(): Int = dataset.length()

}