package org.openmined.syft.data

import org.pytorch.IValue
import org.pytorch.Tensor

/**
 * Wrapper of data and shape
 * @property flattenedArray the actual data
 * @property shape data shape
 * */
data class KTensor(val flattenedArray: FloatArray, val shape: LongArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KTensor

        if (!flattenedArray.contentEquals(other.flattenedArray)) return false
        if (!shape.contentEquals(other.shape)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flattenedArray.contentHashCode()
        result = 31 * result + shape.contentHashCode()
        return result
    }

    fun toIVaule() : IValue {
        return IValue.from(Tensor.fromBlob(flattenedArray, shape))
    }
}