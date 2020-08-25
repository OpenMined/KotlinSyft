package org.openmined.syft.datasource

/**
 * Wraps another sampler to yield a mini-batch of indices.
 * @param sampler (Sampler or Iterable): Base sampler. Can be any iterable object
 * @param batchSize (Int): Size of mini-batch.
 * @param dropLast (Boolean): If ``True``, the sampler will drop the last batch if
 *                          its size would be less than ``batchSize``
 */
class BatchSampler (var sampler: Sampler, var batchSize: Int = 1, var dropLast: Boolean = false){
}