package org.openmined.syft.proto

import com.google.protobuf.ProtocolStringList
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.openmined.syftproto.types.syft.v1.IdOuterClass
import org.openmined.syftproto.types.torch.v1.SizeOuterClass
import org.openmined.syftproto.types.torch.v1.Tensor
import org.openmined.syftproto.types.torch.v1.TensorDataOuterClass

@ExperimentalUnsignedTypes
class SyftTensorTest {

    @Test
    fun `Given a SyftProtoTensor when deserialized it return the expected SyftTensor`() {
        val tensorShape = mock<SizeOuterClass.Size> {
            on { dimsList } doReturn listOf(1, 2)
        }
        val type = "Long"
        val tensorData = mock<TensorDataOuterClass.TensorData> {
            on { shape } doReturn tensorShape
            on { dtype } doReturn type
        }
        val tensorId = mock<IdOuterClass.Id>()

        val protocolTagList = mock<ProtocolStringList>()
        val description = "Description of this tensor"

        val tensor = mock<Tensor.TorchTensor> {
            on { contentsData } doReturn tensorData
            on { id } doReturn tensorId
            on { tagsList } doReturn protocolTagList
            on { this.description } doReturn description
        }

        // When
        val result = tensor.deserialize()

        // Then
        assert(tensorId == result.id)
        assert(tensorData == result.contents)
        assert(listOf(1, 2) == result.shape)
        assert(type == result.dtype)
        assert(protocolTagList == result.tags)
        assert(description == description)
    }
}