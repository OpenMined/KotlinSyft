package org.openmined.syft.data.samplers

import kotlin.math.ceil
import kotlin.math.floor

/**
 * Wraps another sampler to yield a mini-batch of indices.
 * @property indexer (Sampler or Iterable): Base sampler. Can be any iterable object
 * @property batchSize (Int): Size of mini-batch.
 * @property dropLast (Boolean): If ``True``, the sampler will drop the last batch if
 *                          its size would be less than ``batchSize``
 */
class BatchSampler(
    private val indexer: Sampler,
    private val batchSize: Int = 1,
    private val dropLast: Boolean = false
) : Sampler {

    private val mIndices = indexer.indices

    private var currentIndex = 0

    override val indices: List<Int>
        get() = when {
            currentIndex + batchSize < mIndices.size -> {
                val batch = mIndices.slice(currentIndex until currentIndex + batchSize)
                currentIndex += batch.size
                batch
            }
            else -> {
                if (dropLast) {
                    emptyList()
                } else {
                    val batch = mIndices.drop(currentIndex)
                    currentIndex = mIndices.size
                    batch
                }
            }
        }

    override val length: Int = if (dropLast) floor(1.0 * indexer.length / batchSize).toInt()
        else ceil(1.0 * indexer.length / batchSize).toInt()

    fun reset() {
        currentIndex = 0
    }
}
