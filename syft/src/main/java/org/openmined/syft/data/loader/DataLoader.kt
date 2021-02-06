package org.openmined.syft.data.loader

import org.pytorch.IValue

interface DataLoader : Iterable<List<IValue>> {

    fun reset() {}

}