package org.openmined.syft.unit.data

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.openmined.syft.data.BaseDataLoaderIterator
import org.openmined.syft.data.DataLoader
import org.openmined.syft.data.Dataset
import org.openmined.syft.data.KTensor

@ExperimentalUnsignedTypes
class DataLoaderTest {

    private val pair = Pair(
        KTensor(floatArrayOf(1f, 1f), longArrayOf(1, 2)),
        KTensor(floatArrayOf(1f), longArrayOf(1, 1))
    )

    private val dataset = mock<Dataset> {
        on { getItem(any()) }.thenReturn(pair)
        on { length() }.thenReturn(10)
    }

    @Test
    fun `indexSampler returns sequential indices when shuffle is false`() {
        val dataLoader = DataLoader(dataset, batchSize = 3, shuffle = false)
        assert(dataLoader.indexSampler().indices() == listOf(0, 1, 2))
    }

    @Test
    fun `indexSampler returns random indices when shuffle is true`() {
        val dataLoader = DataLoader(dataset, batchSize = 3, shuffle = true)

        val indices = (0 until dataset.length())
        dataLoader.indexSampler().indices().forEach {
            assert(it in indices)
        }
    }

    @Test
    fun `when dropLast is true indexSampler should drop batches not equal to batchSize`() {
        val dataLoader = DataLoader(dataset, batchSize = 3, dropLast = true)

        assert(dataLoader.indexSampler().indices().size == 3)
        assert(dataLoader.indexSampler().indices().size == 3)
        assert(dataLoader.indexSampler().indices().size == 3)
        assert(dataLoader.indexSampler().indices().isEmpty())
    }

    @Test
    fun `when dropLast is false indexSampler should returns all data in batches`() {
        val dataLoader = DataLoader(dataset, batchSize = 3, dropLast = false)

        assert(dataLoader.indexSampler().indices().size == 3)
        assert(dataLoader.indexSampler().indices().size == 3)
        assert(dataLoader.indexSampler().indices().size == 3)
        assert(dataLoader.indexSampler().indices().size == 1)
    }

    @Test
    fun `dataLoader iterator consumes data correctly`() {
        val dataLoader = DataLoader(dataset, batchSize = 3)

        val iterator = dataLoader.iterator()
        assert(iterator.hasNext())
        for (i in 0 until dataLoader.indexSampler().length())
            iterator.next()
        assert(!iterator.hasNext())
    }

    @Test
    fun `BaseIterator consumes data correctly`() {
        val dataLoader = DataLoader(dataset, batchSize = 3)

        val iterator = BaseDataLoaderIterator(dataLoader)
        assert(iterator.hasNext())
        for (i in 0 until dataLoader.indexSampler().length())
            iterator.next()
        assert(!iterator.hasNext())
    }

}
