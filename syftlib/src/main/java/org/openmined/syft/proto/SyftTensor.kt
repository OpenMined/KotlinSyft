package org.openmined.syft.proto

import com.google.protobuf.ProtocolStringList
import org.openmined.syftproto.types.syft.v1.IdOuterClass
import org.openmined.syftproto.types.torch.v1.SizeOuterClass
import org.openmined.syftproto.types.torch.v1.Tensor
import org.openmined.syftproto.types.torch.v1.TensorDataOuterClass
import java.nio.DoubleBuffer
import org.openmined.syftproto.types.torch.v1.Tensor.TorchTensor as SyftProtoTensor
import org.pytorch.Tensor as PytorchTensor

@ExperimentalUnsignedTypes
data class SyftTensor(
    val id: IdOuterClass.Id,
    val contents: TensorDataOuterClass.TensorData,
    val shape: MutableList<Int>,
    val dtype: String,
    val chain: SyftTensor?,
    val grad_chain: SyftTensor?,
    val tags: ProtocolStringList,
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
                tensorData.shape.dimsList,
                tensorData.dtype,
                chain,
                gradChain,
                tensor.tagsList,
                tensor.description
            )
        }

        fun fromTorchTensor(tensor: PytorchTensor): SyftTensor {
            return SyftTensor()
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

    fun getTorchTensor(): PytorchTensor {
        return when (dtype) {
            "Uint8" -> PytorchTensor.fromBlob(
                contents.contentsUint8List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "Int8" -> PytorchTensor.fromBlob(
                contents.contentsInt8List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "Int16" -> PytorchTensor.fromBlob(
                contents.contentsInt16List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "Int32" -> PytorchTensor.fromBlob(
                contents.contentsInt32List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "Int64" -> PytorchTensor.fromBlob(
                contents.contentsInt64List.toLongArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "Float16" -> PytorchTensor.fromBlob(
                contents.contentsFloat16List.toFloatArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "Float32" -> PytorchTensor.fromBlob(
                contents.contentsFloat32List.toFloatArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "Float64" -> PytorchTensor.fromBlob(
                DoubleBuffer.wrap(
                    contents.contentsFloat64List.toDoubleArray()
                ), shape.map { it.toLong() }.toLongArray()
            )
//            "Bool" -> PytorchTensor.fromBlob(
//                contents.data.contentsUint8List.toIntArray(),
//                shape.map { it.toLong() }.toLongArray()
//            )
            "Qint8" -> PytorchTensor.fromBlob(
                contents.contentsQint8List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "Quint8" -> PytorchTensor.fromBlob(
                contents.contentsQuint8List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "Qint32" -> PytorchTensor.fromBlob(
                contents.contentsQint32List.toIntArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            "Bfloat16" -> PytorchTensor.fromBlob(
                contents.contentsBfloat16List.toFloatArray(),
                shape.map { it.toLong() }.toLongArray()
            )
            else -> throw Exception("Invalid Tensor type")
        }

    }
}