package org.openmined.syft.proto

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.openmined.syftproto.types.syft.v1.IdOuterClass
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
    fun `Given a TorchTensor with int32 contents when transform into SyftTensor then returns the expected object`() {
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

    @Test
    fun `Given a TorchTensor with float32 contents when transform into SyftTensor then returns the expected object`() {
        val shape = listOf(1, 2)
        val torchTensor = mockk<TorchTensor>() {
            every { shape() } returns longArrayOf(1, 2)
            every { dtype() } returns DType.FLOAT32
            every { dataAsFloatArray } returns floatArrayOf(100F, 100F, 100F)
        }

        val result = torchTensor.toSyftTensor()

        assert(shape == result.shape)
        assert("float32" == result.dtype)
        assert(listOf(100F, 100F, 100F) == result.contents.contentsFloat32List)
        assert(result.tags.isEmpty())
        assert(result.description.isEmpty())
    }

    @Test
    fun `Given a TorchTensor with int64 contents when transform into SyftTensor then returns the expected object`() {
        val shape = listOf(1, 2)
        val torchTensor = mockk<TorchTensor>() {
            every { shape() } returns longArrayOf(1, 2)
            every { dtype() } returns DType.INT64
            every { dataAsLongArray } returns longArrayOf(100, 100, 100)
        }

        val result = torchTensor.toSyftTensor()

        assert(shape == result.shape)
        assert("int64" == result.dtype)
        assert(listOf(100L, 100L, 100L) == result.contents.contentsInt64List)
        assert(result.tags.isEmpty())
        assert(result.description.isEmpty())
    }

    @Test
    fun `Given a TorchTensor with float64 contents when transform into SyftTensor then returns the expected object`() {
        val shape = listOf(1, 2)
        val torchTensor = mockk<TorchTensor>() {
            every { shape() } returns longArrayOf(1, 2)
            every { dtype() } returns DType.FLOAT64
            every { dataAsDoubleArray } returns doubleArrayOf(100.0, 100.0, 100.0)
        }

        val result = torchTensor.toSyftTensor()

        assert(shape == result.shape)
        assert("float64" == result.dtype)
        assert(listOf(100.0, 100.0, 100.0) == result.contents.contentsFloat64List)
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