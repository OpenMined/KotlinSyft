package org.openmined.syft.unit.data.loader

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.openmined.syft.data.loader.DataLoaderIterator
import org.openmined.syft.data.loader.SyftDataLoader
import org.openmined.syft.data.Dataset
import org.pytorch.IValue
import org.pytorch.Tensor

@ExperimentalUnsignedTypes
class DataLoaderIteratorTest {

    private val list = listOf(
        IValue.from(Tensor.fromBlob(floatArrayOf(1f, 1f), longArrayOf(1, 2))),
        IValue.from(Tensor.fromBlob(floatArrayOf(1f), longArrayOf(1, 1)))
    )

    private val dataset = mock<Dataset> {
        on { getItem(any()) }.thenReturn(list)
        on { length }.thenReturn(10)
    }

    private val indices = (0 until 10).toList()

    @Test
    fun `iterator consumes data correctly`() {
        val dataLoader =
                SyftDataLoader(dataset, batchSize = 3)

        val iterator = DataLoaderIterator(dataLoader)
        assert(iterator.hasNext())
        for (i in 0 until dataLoader.indexSampler.length)
            iterator.next()
        assert(!iterator.hasNext())
    }

    @Test
    fun `reset should set index to 0`() {
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
