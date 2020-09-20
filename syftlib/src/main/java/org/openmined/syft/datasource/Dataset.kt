package org.openmined.syft.datasource

@ExperimentalUnsignedTypes
interface Dataset {
    /**
     * An abstract class representing a :class:`Dataset`.
     * All datasets that represent a map from keys to data samples should subclass
     * it. All subclasses should overwrite :meth:`getitem`, supporting fetching a
     * data sample for a given key. Subclasses could also optionally overwrite
     * :meth:`len`.
     */

    /**
     * This method is called to return the size of the dataset, needs to be overridden.
     */
    fun len() : Int {return  0}

    /**
     * This method is called to fetch a data sample for a given key.
     */
    fun getitem(index: Float) {}


}