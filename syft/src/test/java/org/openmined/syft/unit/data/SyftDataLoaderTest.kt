package org.openmined.syft.unit.data

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.openmined.syft.data.loader.DataLoaderIterator
import org.openmined.syft.data.loader.SyftDataLoader
import org.openmined.syft.data.Dataset
import org.openmined.syft.data.samplers.RandomSampler
import org.pytorch.IValue
import org.pytorch.Tensor
import java.util.Random

@ExperimentalUnsignedTypes
class SyftDataLoaderTest {

    private val list = listOf(
        IValue.from(Tensor.fromBlob(floatArrayOf(1f, 1f), longArrayOf(1, 2))),
        IValue.from(Tensor.fromBlob(floatArrayOf(1f), longArrayOf(1, 1)))
    )

    private val dataset = mock<Dataset> {
        on { getItem(any()) }.thenReturn(list)
        on { length() }.thenReturn(10)
    }

    private val indices = (0 until 10).toList()

    @Test
    fun `indexSampler returns sequential indices when receiving a sequential sampler`() {
        val seqSampler = mock<RandomSampler> {
            on { indices }.thenReturn(indices)
            on { length }.thenReturn(indices.size)
        }

        val dataLoader = SyftDataLoader(
            dataset,
            batchSize = 3,
            sampler = seqSampler
        )
        assert(dataLoader.indexSampler.indices == listOf(0, 1, 2))
    }

    @Test
    fun `indexSampler returns random indices when receiving a random sampler`() {
        val randomSampler = mock<RandomSampler> {
            on { indices }.thenReturn(indices.shuffled(Random()).toList())
            on { length }.thenReturn(indices.size)
        }
        val dataLoader = SyftDataLoader(
            dataset,
            batchSize = 3,
            sampler = randomSampler
        )

        val indices = (0 until dataset.length())
        dataLoader.indexSampler.indices.forEach {
            assert(it in indices)
        }
    }

    @Test
    fun `when dropLast is true indexSampler should drop batches not equal to batchSize`() {
        val dataLoader = SyftDataLoader(
            dataset,
            batchSize = 3,
            dropLast = true
        )

        assert(dataLoader.indexSampler.indices.size == 3)
        assert(dataLoader.indexSampler.indices.size == 3)
        assert(dataLoader.indexSampler.indices.size == 3)
        assert(dataLoader.indexSampler.indices.isEmpty())
    }

    @Test
    fun `when dropLast is false indexSampler should returns all data in batches`() {
        val dataLoader = SyftDataLoader(
            dataset,
            batchSize = 3,
            dropLast = false
        )

        assert(dataLoader.indexSampler.indices.size == 3)
        assert(dataLoader.indexSampler.indices.size == 3)
        assert(dataLoader.indexSampler.indices.size == 3)
        assert(dataLoader.indexSampler.indices.size == 1)
    }

    @Test
    fun `dataLoader iterator consumes data correctly`() {
        val dataLoader =
                SyftDataLoader(dataset, batchSize = 3)

        val iterator = dataLoader.iterator()
        assert(iterator.hasNext())
        for (i in 0 until dataLoader.indexSampler.length)
            iterator.next()
        assert(!iterator.hasNext())
    }

    @Test
    fun `BaseIterator consumes data correctly`() {
        val dataLoader =
                SyftDataLoader(dataset, batchSize = 3)

        val iterator =
                DataLoaderIterator(dataLoader)
        assert(iterator.hasNext())
        for (i in 0 until dataLoader.indexSampler.length)
            iterator.next()
        assert(!iterator.hasNext())
    }

    @Test
    fun `reset should reset the dataloader to the first index`() {
        val dataLoader =
                SyftDataLoader(dataset, batchSize = 3)

        val iterator = dataLoader.iterator()
        assert(iterator.hasNext())
        for (i in 0 until dataLoader.indexSampler.length)
            iterator.next()
        assert(!iterator.hasNext())

        dataLoader.reset()
        assert(iterator.hasNext())
    }
}
