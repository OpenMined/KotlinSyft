package org.openmined.syft.unit.data.samplers

import org.junit.Assert
import org.junit.Test
import org.openmined.syft.data.samplers.SequentialSampler
import org.openmined.syft.unit.data.DatasetTest

@ExperimentalUnsignedTypes
class SquentialSamplerTest {

    private val datasetTest = DatasetTest()

    private val sampler = SequentialSampler(datasetTest)

    @Test
    fun `indices should return sequential indices from 0 until the dataset length`() {
        Assert.assertEquals(sampler.indices(), (0 until datasetTest.length()).toList())
    }

    @Test
    fun `length should return the same length as the dataset length`() {
        assert(sampler.length() == datasetTest.length())
    }

}