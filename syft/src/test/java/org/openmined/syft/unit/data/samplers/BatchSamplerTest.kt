package org.openmined.syft.unit.data.samplers

import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.openmined.syft.data.samplers.BatchSampler
import org.openmined.syft.data.samplers.RandomSampler
import java.util.Random
import kotlin.math.ceil
import kotlin.math.floor

@ExperimentalUnsignedTypes
class BatchSamplerTest {

    private val datasetLength = 10

    private val indices = (0 until datasetLength).toList()

    private val batchSize = 3

    private val randomSampler = mock<RandomSampler> {
        on { indices }.thenReturn(indices.shuffled(Random()).toList())
        on { length }.thenReturn(indices.size)
    }

    private val seqSampler = mock<RandomSampler> {
        on { indices }.thenReturn(indices)
        on { length }.thenReturn(indices.size)
    }

    @Test
    fun `given sequential sampler, indices should return sequential batch with length of batch size`() {
        val sampler = BatchSampler(seqSampler, batchSize)
        val firstBatch = sampler.indices
        assert(firstBatch.size == batchSize)
        assert(firstBatch == listOf(0, 1, 2))
    }

    @Test
    fun `given random sampler, indices should return random batch with length of batch size`() {
        val sampler = BatchSampler(randomSampler, batchSize)
        val firstBatch = sampler.indices
        assert(firstBatch.size == batchSize)
        assert(firstBatch.all { it in indices })
    }

    @Test
    fun `given sequential or random sampler with dropLast enabled, indices should only return batches equal to the batch size`() {
        val sampler = BatchSampler(seqSampler, batchSize, true)
        for (i in 0 until 3) sampler.indices
        assert(sampler.indices.isEmpty())
    }

    @Test
    fun `length should return the quotient of dividing dataset length by batchSize`() {
        assert(BatchSampler(seqSampler, batchSize).length == ceil(1.0 * seqSampler.length / batchSize)
                .toInt())
        assert(BatchSampler(seqSampler, batchSize, true).length == floor(1.0 * seqSampler.length/batchSize).toInt())
    }

    @Test
    fun `reset should reset current index to 0 and return the first indices`() {
        val sampler = BatchSampler(seqSampler, batchSize, true)
        val indices = sampler.indices
        assert(indices == listOf(0, 1, 2))
        sampler.reset()
        assert(indices == listOf(0, 1, 2))
    }

}
