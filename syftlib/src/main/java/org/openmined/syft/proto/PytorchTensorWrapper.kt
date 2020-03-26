package org.openmined.syft.proto

import org.pytorch.DType
import org.pytorch.Tensor
import java.io.InvalidClassException
import java.nio.DoubleBuffer

class PytorchTensorWrapper(val torchTensor: Tensor) {

    @ExperimentalUnsignedTypes
    operator fun minus(value: PytorchTensorWrapper): PytorchTensorWrapper {
        return PytorchTensorWrapper(this.torchTensor - value.torchTensor)
    }

    @ExperimentalUnsignedTypes
    private operator fun Tensor.minus(value: Tensor): Tensor {
        return when (this.dtype()) {
            DType.UINT8 -> Tensor.fromBlob(
                this.dataAsUnsignedByteArray - value.dataAsUnsignedByteArray,
                this.shape()
            )
            DType.INT8 -> Tensor.fromBlob(
                this.dataAsByteArray - value.dataAsByteArray,
                this.shape()
            )
            DType.INT32 -> Tensor.fromBlob(
                this.dataAsIntArray - value.dataAsIntArray,
                this.shape()
            )
            DType.FLOAT32 -> Tensor.fromBlob(
                this.dataAsFloatArray - value.dataAsFloatArray,
                this.shape()
            )
            DType.INT64 -> Tensor.fromBlob(
                this.dataAsLongArray - value.dataAsLongArray,
                this.shape()
            )
            DType.FLOAT64 -> Tensor.fromBlob(
                DoubleBuffer.wrap(this.dataAsDoubleArray - value.dataAsDoubleArray),
                this.shape()
            )
            else -> throw InvalidClassException("Dtype does not exist")
        }
    }

    @ExperimentalUnsignedTypes
    private inline operator fun <reified T> Array<T>.minus(value: Array<T>): Array<T> {
        return this.zip(value).map { it.first - it.second }.toTypedArray()
    }

    @ExperimentalUnsignedTypes
    private inline operator fun <reified T> T.minus(second: T): T {
        return when (T::class) {
            Int::class -> (this as Int) - (second as Int)
            UInt::class -> (this as UInt) - (second as UInt)
            Byte::class -> (this as Byte) - (second as Byte)
            Float::class -> (this as Float) - (second as Float)
            Long::class -> (this as Long) - (second as Long)
            Double::class -> (this as Double) - (second as Double)
            else -> throw InvalidClassException("substraction of incompatible types")
        } as T
    }
}