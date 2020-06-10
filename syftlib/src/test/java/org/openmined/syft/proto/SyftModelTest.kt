package org.openmined.syft.proto

import org.junit.Test

@ExperimentalUnsignedTypes
internal class SyftModelTest {

    /**
     * This test also verifies the correct serialization to State and Placeholders
     */
    @Test
    fun `given a model param file test it correctly serialises to SyftModel`() {
        val syftModel = SyftModel("model name", "1.0.0-version")
        val filePath = javaClass.classLoader?.getResource("proto_files/model_params.pb")?.path
        assert(filePath != null)
        filePath?.let {
            syftModel.loadModelState(it)
            assert(syftModel.modelState?.syftTensors?.size == 4)
            assert(syftModel.modelState?.placeholders?.size == 4)
        }
    }
}