package org.openmined.syft.unit.execution.checkpoint

import com.nhaarman.mockitokotlin2.mock
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.execution.checkpoint.CheckPoint
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.datamodels.ClientProperties

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class ModelCheckPointTest {

    val clientConfig = ClientConfig(
        ClientProperties("test", "test", 1),
        mutableMapOf("batch_size" to "1")
    )

    val job = mock<SyftJob> {
        on { clientConfig }.thenReturn(clientConfig)
        on { currentStep }.thenReturn(1)
        on { jobModel }.thenReturn(mockk())
        on { model }.thenReturn(mockk())
    }

    @Test
    fun `checkpoint can be created from syftjob`() {
        val checkPoint = CheckPoint.fromJob(job)
        assert(checkPoint.currentStep == 1)
        assert(checkPoint.clientConfig == clientConfig)
    }

}