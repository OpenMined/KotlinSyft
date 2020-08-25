package org.openmined.syft.datasource

/**
 * Samples elements sequentially, always in the same order.
 * @param dataSource (Dataset): dataset to sample from
 */
class SequentialSampler(override var dataSource: Dataset) : Sampler(dataSource) {

    override fun iter(){}

    override fun len() : Int{
        return  dataSource.len()
    }
}