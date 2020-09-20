package org.openmined.syft.datasource

/**
 * Samples elements sequentially, always in the same order.
 * @param dataSource (Dataset): dataset to sample from
 */
class SequentialSampler(val dataSource: Dataset) : Dataset, Sampler{

    override fun iter() {}

    override fun len(): Int {return 0}
}