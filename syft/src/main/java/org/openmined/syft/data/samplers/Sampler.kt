package org.openmined.syft.data.samplers

/**
 * Base class for all Samplers.
 * Every Sampler subclass has to provide an :method:`iter` method, providing a
 * way to iterate over indices of dataset elements, and a :method:`length` method
 * that returns the length of the returned iterators.
 */
interface Sampler {

    fun iter(): List<Int>

    fun length(): Int
}