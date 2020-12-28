package org.openmined.syft.unit.data.samplers

import org.junit.Test
import org.openmined.syft.data.samplers.RandomSampler
import org.openmined.syft.unit.data.TestDataset

@ExperimentalUnsignedTypes
class RandomSamplerTest {

    private val datasetTest = TestDataset()

    private val sampler = RandomSampler(datasetTest)

    @Test
    fun `indices should return random indices from 0 until the dataset length`() {
        val indices = (0 until datasetTest.length())
        sampler.indices().forEach {
            assert(it in indices)
        }

    }

    @Test
    fun `length should return the same length as the dataset length`() {
        assert(sampler.length() == datasetTest.length())
    }

}