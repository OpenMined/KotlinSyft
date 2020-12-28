package org.openmined.syft.data.fetchers

import org.pytorch.IValue

interface Fetcher {

    fun fetch(indices: List<Int>): Pair<IValue, IValue>

}