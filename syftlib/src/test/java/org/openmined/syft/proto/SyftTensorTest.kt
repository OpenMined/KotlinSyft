package org.openmined.syft.proto

import com.nhaarman.mockitokotlin2.any
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.openmined.syftproto.types.syft.v1.IdOuterClass
import org.openmined.syftproto.types.torch.v1.SizeOuterClass
import org.openmined.syftproto.types.torch.v1.Tensor
import org.openmined.syftproto.types.torch.v1.TensorDataOuterClass
import org.pytorch.DType
import org.pytorch.Tensor as TorchTensor

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
    }

    @Test
    fun `Given a SyftProtoTensor when deserialized it return the expected SyftTensor`() {
        every { tensorData.shape.dimsList } returns listOf(1, 2)
        every { tensorData.dtype } returns type

        val desc = "I'm your father"
        val tensor = createSyftProtoTensor(hasChain = true, hasGradChain = true, desc = desc)
        // When
        val result = tensor.toSyftTensor()

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

    @Test
    fun `Given a TorchTensor when transform into SyftTensor then returns the expected object`() {
        val shape = listOf(1, 2)
        val torchTensor = mockk<TorchTensor>() {
            every { shape() } returns longArrayOf(1, 2)
            every { dtype() } returns DType.INT32
            every { dataAsIntArray } returns intArrayOf(100, 100, 100)
        }

        val result = torchTensor.toSyftTensor()

        assert(shape == result.shape)
        assert("int32" == result.dtype)
        assert(listOf(100, 100, 100) == result.contents.contentsInt32List)
        assert(result.tags.isEmpty())
        assert(result.description.isEmpty())
    }

    private fun createSyftProtoTensor(
        hasChain: Boolean,
        hasGradChain: Boolean,
        desc: String
    ): Tensor.TorchTensor {

        val builder = Tensor.TorchTensor.newBuilder()
                .setId(tensorId)
                .setContentsData(tensorData)
                .addTags("myTag")
                .setDescription(desc)

        if (hasChain) {
            builder.chain = createSyftProtoTensor(
                hasChain = false,
                hasGradChain = false,
                desc = "I'm your child"
            )
        }
        if (hasGradChain) {
            builder.gradChain = createSyftProtoTensor(
                hasChain = false,
                hasGradChain = false,
                desc = "I'm your graduated child"
            )
        }
        return builder.build()
    }
}