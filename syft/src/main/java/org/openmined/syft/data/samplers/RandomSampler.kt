package org.openmined.syft.data.samplers

import org.openmined.syft.data.Dataset
import java.util.Random


/**
 * Samples elements randomly. If without replacement, then sample from a shuffled dataset.
 * If with replacement, then user can specify :attr:`num_samples` to draw.
 * @property dataset (Dataset): dataset to sample from
 */
class RandomSampler(private val dataset: Dataset) :
    Sampler {

    override fun iter() = (0 until  dataset.length()).shuffled(Random()).toList()

    override fun length(): Int = dataset.length()

}