package org.openmined.syft.proto

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Before
import org.junit.Test
import org.openmined.syftproto.types.syft.v1.IdOuterClass
import org.openmined.syftproto.types.torch.v1.Tensor
import org.openmined.syftproto.types.torch.v1.TensorDataOuterClass

@ExperimentalUnsignedTypes
class SyftTensorTest {

    private val type = "TensorType"

    @RelaxedMockK
    private lateinit var tensorData: TensorDataOuterClass.TensorData
    @RelaxedMockK
    private lateinit var tensorId: IdOuterClass.Id

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { tensorData.shape.dimsList } returns listOf(1, 2)
        every { tensorData.dtype } returns type
    }

    @Test
    fun `Given a SyftProtoTensor when deserialized it return the expected SyftTensor`() {
        val desc = "I'm your father"
        val tensor = createTorchTensor(hasChain = true, hasGradChain = true, desc = desc)
        // When
        val result = tensor.deserialize()

        // Then
        assert(tensorId == result.id)
        assert(tensorData == result.contents)
        assert(listOf(1, 2) == result.shape)
        assert(type == result.dtype)
        assert(listOf("myTag") == result.tags)
        assert(desc == result.description)
        assert(result.chain != null)
        assert(result.grad_chain != null)
    }

    private fun createTorchTensor(hasChain: Boolean, hasGradChain: Boolean, desc: String): Tensor.TorchTensor {

        val builder = Tensor.TorchTensor.newBuilder()
                .setId(tensorId)
                .setContentsData(tensorData)
                .addTags("myTag")
                .setDescription(desc)

        if (hasChain) {
            builder.chain = createTorchTensor(
                hasChain = false,
                hasGradChain = false,
                desc = "I'm your child"
            )
        }
        if (hasGradChain) {
            builder.gradChain = createTorchTensor(
                hasChain = false,
                hasGradChain = false,
                desc = "I'm your graduated child"
            )
        }
        return builder.build()
    }
}