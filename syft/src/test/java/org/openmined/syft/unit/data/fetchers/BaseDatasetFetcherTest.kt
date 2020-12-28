package org.openmined.syft.unit.data.fetchers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.openmined.syft.data.Dataset
import org.openmined.syft.data.KTensor
import org.openmined.syft.data.fetchers.BaseDatasetFetcher

@ExperimentalUnsignedTypes
class BaseDatasetFetcherTest {

    private val pair = Pair(
        KTensor(floatArrayOf(1f, 1f), longArrayOf(1, 2)),
        KTensor(floatArrayOf(1f), longArrayOf(1, 1))
    )

    private val dataset = mock<Dataset> {
        on { getItem(any()) }.thenReturn(pair)
        on { length() }.thenReturn(10)
    }

    private val fetcher = BaseDatasetFetcher(dataset)

    @Test
    fun `verify fetcher should call dataset getItem`() {
        fetcher.fetch(listOf(0, 1, 2))
        verify(dataset, times(3)).getItem(any())
    }

}
