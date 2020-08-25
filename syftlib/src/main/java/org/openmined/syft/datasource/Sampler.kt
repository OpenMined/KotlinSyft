package org.openmined.syft.datasource

/**
 * Base class for all Samplers.
 * Every Sampler subclass has to provide an :meth:`__iter__` method, providing a
 * way to iterate over indices of dataset elements, and a :meth:`__len__` method
 * that returns the length of the returned iterators.
 */
open class Sampler (open var dataSource: Dataset){
    open fun iter() {}
    open fun len() {}
}