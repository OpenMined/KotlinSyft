package org.openmined.syft.unit.proto

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.openmined.syft.proto.SyftModel
import org.openmined.syft.proto.SyftState
import org.openmined.syft.proto.SyftTensor
import org.openmined.syft.proto.toSyftTensor
import org.pytorch.IValue
import org.pytorch.Tensor
import java.lang.IllegalStateException

@ExperimentalUnsignedTypes
internal class SyftModelTest {

    private lateinit var cut: SyftModel
    private lateinit var modelFilePath: String

    @Before
    fun setUp() {
        cut = SyftModel("model name", "1.0.0-version")
        val filePath = javaClass.classLoader?.getResource("proto_files/model_params.pb")?.path
        assert(filePath != null)
        modelFilePath = filePath!!
    }

    /**
     * This test also verifies the correct serialization to State and Placeholders
     */
    @Test
    fun `given a model param file test it correctly serialises to SyftModel`() {
        cut.loadModelState(modelFilePath)
        assert(cut.modelSyftState?.syftTensorArray?.size == 4)
        assert(cut.modelSyftState?.placeholders?.size == 4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given a list of params when model is updated and tensor list size does not match then an exception is thrown`() {
        // Given
        mockkStatic("org.openmined.syft.proto.SyftTensorKt")
        val syftIValue1 = mockk<IValue>()
        val syftIValue2 = mockk<IValue>()
        val syftIValue3 = mockk<IValue>()
        val params = listOf(syftIValue1, syftIValue2, syftIValue3)

        cut.loadModelState(modelFilePath)

        // When
        cut.updateModel(params)
    }

    @Test
    fun `createDiff calls the the diffing method of SyftState if modelState is not null`() {
        val mockOldState = mockk<SyftState>()
        val mockCurrentState = mockk<SyftState> {
            every { createDiff(mockOldState, "script location") } returns mockk()
        }
        val cut = SyftModel("test", modelSyftState = mockCurrentState)
        cut.createDiff(mockOldState, "script location")
        verify { mockCurrentState.createDiff(mockOldState, "script location") }
    }

    @Test
    fun `getParamsIValueArray calls syftState's getIValueTensorArray`(){
        val returnIvalue = arrayOf(Tensor.fromBlob(longArrayOf(0L), longArrayOf(1)))
        val state = mockk<SyftState>{
            every { tensorArray } returns returnIvalue
        }
        val cut = SyftModel("model name",modelSyftState = state)
        val out = cut.paramArray
        verify { state.tensorArray }
        assert(out?.contentEquals(returnIvalue) ?: false)
    }


    @Test
    fun `getParamsIValueArray return null if state is null`(){
        val cut = SyftModel("test")
        val out = cut.paramArray
        assert(out == null)
    }

    @Test(expected = IllegalStateException::class)
    fun `createDiff throws error if modelState is null`() {
        val mockOldState = mockk<SyftState>()
        val cut = SyftModel("test","1.0.0")
        cut.createDiff(mockOldState, "script location")
    }

    @Test
    fun `Given a list of params when model is updated then model state is updated`() {
        // Given
        mockkStatic("org.openmined.syft.proto.SyftTensorKt")
        val syftTensor1 = mockk<IValue>()
        val syftTensor2 = mockk<IValue>()
        val syftTensor3 = mockk<IValue>()
        val syftTensor4 = mockk<IValue>()
        val syftTensors = arrayOf(syftTensor1, syftTensor2, syftTensor3, syftTensor4)

        val params = listOf(syftTensor1, syftTensor2, syftTensor3, syftTensor4)

        cut.loadModelState(modelFilePath)

        // When
        cut.updateModel(params)

        // Then
        assert(params.size == cut.stateTensorSize)
        assert(cut.modelSyftState?.iValueTensors?.contentEquals(syftTensors) == true)
    }
}