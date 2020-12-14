package org.openmined.syft.unit.data.fetchers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.openmined.syft.data.Dataset
import org.openmined.syft.data.fetchers.MapDatasetFetcher
import org.pytorch.IValue

@ExperimentalUnsignedTypes
class MapDatasetFetcherTest {

    private val dataset = mock<Dataset> {
        on { getItem(any()) }.thenReturn(mock())
        on { length() }.thenReturn(10)
    }

    private val fetcher = MapDatasetFetcher(dataset, false)

}