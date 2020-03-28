package org.openmined.syft.proto

import org.openmined.syftproto.types.syft.v1.IdOuterClass
import org.openmined.syftproto.types.torch.v1.SizeOuterClass
import org.openmined.syftproto.types.torch.v1.Tensor
import org.openmined.syftproto.types.torch.v1.TensorDataOuterClass
import org.pytorch.DType
import java.io.InvalidClassException
import java.lang.Math.random
import java.nio.DoubleBuffer
import java.util.Locale
import org.openmined.syftproto.types.torch.v1.Tensor.TorchTensor as SyftProtoTensor
import org.pytorch.Tensor as TorchTensor

@ExperimentalUnsignedTypes
data class SyftTensor(
    val id: IdOuterClass.Id,
    val contents: TensorDataOuterClass.TensorData,
    val shape: MutableList<Int>,
    val dtype: String,
    val chain: SyftTensor? = null,
    val grad_chain: SyftTensor? = null,
    val tags: List<String>,
    val description: String
) {
    companion object {
        fun deserialize(tensor: SyftProtoTensor): SyftTensor {
            val tensorData = tensor.contentsData
            val chain = if (tensor.hasChain())
                deserialize(tensor.chain)
            else
                null
            val gradChain = if (tensor.hasGradChain())
                deserialize(tensor.gradChain)
            else
                null
            return SyftTensor(
                tensor.id,
                tensorData,
                //todo we should ideally have long here
                tensorData.shape.dimsList,
                tensorData.dtype,
                chain,
                gradChain,
                tensor.tagsList,
                tensor.description
            )
        }

        fun fromTorchTensor(tensor: TorchTensor): SyftTensor {
            val shape = SizeOuterClass.Size.newBuilder()
                    .addAllDims(tensor.shape().map { it.toInt() })
                    .build()
            val tensorDataBuilder = TensorDataOuterClass.TensorData.newBuilder()
                    .setShape(shape)
                    .setDtype(tensor.dtype().name.toLowerCase(Locale.US))
            val tensorData = tensorDataBuilderFromTorchTensor(
                tensor,
                tensorDataBuilder
            ).build()
            val id = IdOuterClass.Id.newBuilder().setIdInt(random().toLong()).build()
            return SyftTensor(
                id,
                tensorData,
                shape.dimsList,
                tensorData.dtype,
                tags = listOf(),
                description = ""
            )
        }

        private fun tensorDataBuilderFromTorchTensor(
            tensor: TorchTensor,
            tensorBuilder: TensorDataOuterClass.TensorData.Builder
        ): TensorDataOuterClass.TensorData.Builder {
            when (tensor.dtype()) {
//                todo Uint8 is converted to int below due to no Pytorch constructor `fromBlob`
//                DType.UINT8 ->
//                    tensorBuilder.addAllContentsUint8(tensor.dataAsUnsignedByteArray)
//                todo Int8 protobuf implementation uses List<Int> losing all the memory efficiency
//                DType.INT8 ->
//                    tensorBuilder.addAllContentsInt8(tensor.dataAsByteArray)
                DType.INT32 -> tensorBuilder.addAllContentsInt32(tensor.dataAsIntArray.toList())
                DType.FLOAT32 -> tensorBuilder.addAllContentsFloat32(tensor.dataAsFloatArray.toList())
                DType.INT64 -> tensorBuilder.addAllContentsInt64(tensor.dataAsLongArray.toList())
                DType.FLOAT64 -> tensorBuilder.addAllContentsFloat64(tensor.dataAsDoubleArray.toList())
                else -> throw InvalidClassException("Dtype does not exist")
            }
            return tensorBuilder
        }
    }

    fun serialize(): SyftProtoTensor {
        SizeOuterClass.Size.newBuilder().addAllDims(shape)
        val syftTensorBuilder = SyftProtoTensor.newBuilder()
                .addAllTags(tags).setId(id)
                .setContentsData(contents)
                .setDescription(description)
                .setSerializer(Tensor.TorchTensor.Serializer.SERIALIZER_ALL)
        if (chain != null)
            syftTensorBuilder.chain = chain.serialize()
        if (grad_chain != null)
            syftTensorBuilder.gradChain = grad_chain.serialize()
        syftTensorBuilder.contentsData = contents
        return syftTensorBuilder.build()
    }

    fun getTorchTensor(): TorchTensor {
        return when (dtype) {
            "uint8" -> TorchTensor.fromBlob(
                contents.contentsUint8List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "int8" -> TorchTensor.fromBlob(
                contents.contentsInt8List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "int16" -> TorchTensor.fromBlob(
                contents.contentsInt16List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "int32" -> TorchTensor.fromBlob(
                contents.contentsInt32List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "int64" -> TorchTensor.fromBlob(
                contents.contentsInt64List.toLongArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "float16" -> TorchTensor.fromBlob(
                contents.contentsFloat16List.toFloatArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "float32" -> TorchTensor.fromBlob(
                contents.contentsFloat32List.toFloatArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "float64" -> TorchTensor.fromBlob(
                DoubleBuffer.wrap(
                    contents.contentsFloat64List.toDoubleArray()
                ), shape.map { it.toLong() }.toLongArray()
            )
//            "Bool" -> org.pytorch.Tensor.fromBlob(
//                contents.data.contentsUint8List.toIntArray(),
//                shape.map { it.toLong() }.toLongArray()
//            )
            "qint8" -> TorchTensor.fromBlob(
                contents.contentsQint8List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "quint8" -> TorchTensor.fromBlob(
                contents.contentsQuint8List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "qint32" -> TorchTensor.fromBlob(
                contents.contentsQint32List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "bfloat16" -> TorchTensor.fromBlob(
                contents.contentsBfloat16List.toFloatArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            else -> throw Exception("Invalid Tensor type")
        }
    }

}