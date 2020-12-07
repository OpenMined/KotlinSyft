package org.openmined.syft.data.fetchers

import org.openmined.syft.data.Dataset

open class BaseDatasetFetcher(val dataset: Dataset, val dropLast: Boolean) : Fetcher