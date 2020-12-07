package org.openmined.syft.data.fetchers

interface Fetcher {

    fun fetch(indices: List<Int>): Any { return Any() }

}