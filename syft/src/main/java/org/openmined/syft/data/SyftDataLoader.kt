package org.openmined.syft.data

import org.pytorch.IValue

/**
 * Tag interface
 */
interface SyftDataLoader : Iterable<Pair<IValue, IValue>>
