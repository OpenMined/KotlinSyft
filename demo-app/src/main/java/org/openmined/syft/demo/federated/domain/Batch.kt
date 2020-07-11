package org.openmined.syft.demo.federated.domain

data class Batch(val flattenedArray: FloatArray, val shape: LongArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Batch

        if (!flattenedArray.contentEquals(other.flattenedArray)) return false
        if (!shape.contentEquals(other.shape)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flattenedArray.contentHashCode()
        result = 31 * result + shape.contentHashCode()
        return result
    }
}