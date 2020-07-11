package org.openmined.syft.unit.proto

import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.Before
import org.junit.Test
import org.openmined.syft.proto.SyftTensor
import org.openmined.syft.proto.toSyftTensor
import org.openmined.syftproto.types.syft.v1.IdOuterClass
import org.openmined.syftproto.types.torch.v1.Tensor
import org.openmined.syftproto.types.torch.v1.TensorDataOuterClass
import org.pytorch.DType
import org.pytorch.IValue
import org.pytorch.Module
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

    @Test
    fun `Given a SyftTensor when it is serialized it returns the corresponding syftProtoTensor`() {
        val tagList = listOf("tag1", "tag2")
        val description = "description"
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "INT32"
        }

        val cut = SyftTensor(
            id = "id",
            contents = tensorData,
            shape = mutableListOf(1, 2),
            dtype = "int32",
            chain = null,
            grad_chain = null,
            tags = tagList,
            description = description
        )
        val syftProtoTensor = cut.serialize()

        assert(syftProtoTensor.contentsData == tensorData)
        assert(syftProtoTensor.id.idStr == "id")
        assert(syftProtoTensor.contentsData.dtype == "INT32")
        assert(syftProtoTensor.tagsList == tagList)
        assert(syftProtoTensor.description == description)
        assert(syftProtoTensor.serializer == Tensor.TorchTensor.Serializer.SERIALIZER_ALL)
    }

    @Test
    fun `Given a SyftTensor when getTorchTensor then the type int32 is used to extract the embedded tensor`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "INT32"
            every { contentsInt32List } returns listOf(100, 100, 100)
        }

        val cut = createSyftTensorFromType("int32", tensorData)

        val torchTensor = cut.getTorchTensor()

        assert(listOf(100, 100, 100) == torchTensor.dataAsIntArray.toList())
        assert(3L == torchTensor.shape()[0])
        assert(1L == torchTensor.shape()[1])
    }

    @Test
    fun `Given a SyftTensor when getTorchTensor then the type uint8 is used to extract the embedded tensor`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "UINT8"
            every { contentsUint8List } returns listOf(100, 100, 100)
        }

        val cut = createSyftTensorFromType("uint8", tensorData)

        val torchTensor = cut.getTorchTensor()

        assert(listOf(100, 100, 100) == torchTensor.dataAsIntArray.toList())
        assert(3L == torchTensor.shape()[0])
        assert(1L == torchTensor.shape()[1])
    }

    @Test
    fun `Given a SyftTensor when getTorchTensor then the type int8 is used to extract the embedded tensor`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "UINT8"
            every { contentsInt8List } returns listOf(100, 100, 100)
        }

        val cut = createSyftTensorFromType("int8", tensorData)

        val torchTensor = cut.getTorchTensor()

        assert(listOf(100, 100, 100) == torchTensor.dataAsIntArray.toList())
        assert(3L == torchTensor.shape()[0])
        assert(1L == torchTensor.shape()[1])
    }

    @Test
    fun `Given a SyftTensor when getTorchTensor then the type int16 is used to extract the embedded tensor`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "INT16"
            every { contentsInt16List } returns listOf(100, 100, 100)
        }

        val cut = createSyftTensorFromType("int16", tensorData)

        val torchTensor = cut.getTorchTensor()

        assert(listOf(100, 100, 100) == torchTensor.dataAsIntArray.toList())
        assert(3L == torchTensor.shape()[0])
        assert(1L == torchTensor.shape()[1])
    }

    @Test
    fun `Given a SyftTensor when getTorchTensor then the type int64 is used to extract the embedded tensor`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "INT64"
            every { contentsInt64List } returns listOf(100, 100, 100)
        }

        val cut = createSyftTensorFromType("int64", tensorData)

        val torchTensor = cut.getTorchTensor()

        assert(listOf(100L, 100L, 100L) == torchTensor.dataAsLongArray.toList())
        assert(3L == torchTensor.shape()[0])
        assert(1L == torchTensor.shape()[1])
    }

    @Test
    fun `Given a SyftTensor when getTorchTensor then the type float16 is used to extract the embedded tensor`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "FLOAT16"
            every { contentsFloat16List } returns listOf(100F, 100F, 100F)
        }

        val cut = createSyftTensorFromType("float16", tensorData)

        val torchTensor = cut.getTorchTensor()

        assert(listOf(100F, 100F, 100F) == torchTensor.dataAsFloatArray.toList())
        assert(3L == torchTensor.shape()[0])
        assert(1L == torchTensor.shape()[1])
    }

    @Test
    fun `Given a SyftTensor when getTorchTensor then the type float32 is used to extract the embedded tensor`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "FLOAT32"
            every { contentsFloat32List } returns listOf(100F, 100F, 100F)
        }

        val cut = createSyftTensorFromType("float32", tensorData)

        val torchTensor = cut.getTorchTensor()

        assert(listOf(100F, 100F, 100F) == torchTensor.dataAsFloatArray.toList())
        assert(3L == torchTensor.shape()[0])
        assert(1L == torchTensor.shape()[1])
    }

    @Test
    fun `Given a SyftTensor when getTorchTensor then the type qint8 is used to extract the embedded tensor`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "QINT8"
            every { contentsQint8List } returns listOf(100, 100, 100)
        }

        val cut = createSyftTensorFromType("qint8", tensorData)

        val torchTensor = cut.getTorchTensor()

        assert(listOf(100, 100, 100) == torchTensor.dataAsIntArray.toList())
        assert(3L == torchTensor.shape()[0])
        assert(1L == torchTensor.shape()[1])
    }

    @Test
    fun `Given a SyftTensor when getTorchTensor then the type quint8 is used to extract the embedded tensor`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "QUINT8"
            every { contentsQuint8List } returns listOf(100, 100, 100)
        }

        val cut = createSyftTensorFromType("quint8", tensorData)

        val torchTensor = cut.getTorchTensor()

        assert(listOf(100, 100, 100) == torchTensor.dataAsIntArray.toList())
        assert(3L == torchTensor.shape()[0])
        assert(1L == torchTensor.shape()[1])
    }

    @Test
    fun `Given a SyftTensor when getTorchTensor then the type qint32 is used to extract the embedded tensor`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "QINT32"
            every { contentsQint32List } returns listOf(100, 100, 100)
        }

        val cut = createSyftTensorFromType("qint32", tensorData)

        val torchTensor = cut.getTorchTensor()

        assert(listOf(100, 100, 100) == torchTensor.dataAsIntArray.toList())
        assert(3L == torchTensor.shape()[0])
        assert(1L == torchTensor.shape()[1])
    }

    @Test
    fun `Given a SyftTensor when getTorchTensor then the type bfloat16 is used to extract the embedded tensor`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "BFLOAT16"
            every { contentsBfloat16List } returns listOf(100F, 100F, 100F)
        }

        val cut = createSyftTensorFromType("bfloat16", tensorData)

        val torchTensor = cut.getTorchTensor()

        assert(listOf(100F, 100F, 100F) == torchTensor.dataAsFloatArray.toList())
        assert(3L == torchTensor.shape()[0])
        assert(1L == torchTensor.shape()[1])
    }

    @Test(expected = Exception::class)
    fun `Given a SyftTensor when getTorchTensor with a non-existant type then an exception is thrown`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "IM_NOT_A_TYPE"
            every { contentsBfloat16List } returns listOf(100F, 100F, 100F)
        }

        val cut = createSyftTensorFromType("I'm not a type!", tensorData)

        cut.getTorchTensor()
    }

    @Test
    fun `applyOperation calls the module forward with relevant arguments and returns syftTensor`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "BFLOAT16"
            every { contentsBfloat16List } returns listOf(100F, 100F, 100F)
        }

        val tensor = createSyftTensorFromType("bfloat16", tensorData)

        val opIvalue1 = mockk<IValue>()
        val opTensor1 = mockk<SyftTensor> {
            every { getIValue() } returns opIvalue1
        }
        val opIvalue2 = mockk<IValue>()
        val opTensor2 = mockk<SyftTensor> {
            every { getIValue() } returns opIvalue2
        }
        val opIvalue3 = mockk<IValue>()
        val opTensor3 = mockk<SyftTensor> {
            every { getIValue() } returns opIvalue3
        }
        val outputTensor = mockk<org.pytorch.Tensor>{
            every { shape() } returns listOf(3L).toLongArray()
            every { dtype() } returns DType.FLOAT32
            every { dataAsFloatArray } returns listOf(100F, 100F, 100F).toFloatArray()
        }
        val outputIvalue = mockk<IValue> {
            every { toTensor() } returns outputTensor
        }

        val cut = spyk(tensor)
        val diffModule = mockk<Module> {
            every {
                forward(
                    opIvalue1,
                    opIvalue2,
                    opIvalue3,
                    cut.getIValue()
                    )
            } returns outputIvalue

        }

        mockkStatic(Module::class)
        every { Module.load(any()) } returns diffModule
        cut.applyOperation("filepath", opTensor1, opTensor2, opTensor3)
    }

    @Test
    fun `Given a SyftTensor getIValue calls the getTorchTensor internally`() {
        val tensorData = mockk<TensorDataOuterClass.TensorData> {
            every { dtype } returns "BFLOAT16"
            every { contentsBfloat16List } returns listOf(100F, 100F, 100F)
        }

        val cut = spy(
            SyftTensor(
                id = "id",
                contents = tensorData,
                shape = mutableListOf(3, 1),
                dtype = "bfloat16",
                chain = null,
                grad_chain = null,
                tags = mockk(),
                description = "description"
            )
        )
        cut.getIValue()
        verify(cut).getTorchTensor()
    }

    private fun createSyftTensorFromType(
        dtype: String,
        tensorData: TensorDataOuterClass.TensorData
    ): SyftTensor {
        return SyftTensor(
            id = "id",
            contents = tensorData,
            shape = mutableListOf(3, 1),
            dtype = dtype,
            chain = null,
            grad_chain = null,
            tags = mockk(),
            description = "description"
        )
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