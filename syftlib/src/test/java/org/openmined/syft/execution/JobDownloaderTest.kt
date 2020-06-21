package org.openmined.syft.execution

import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel

@ExperimentalUnsignedTypes
class JobDownloaderTest {

    @Mock
    private lateinit var configuration: SyftConfiguration

    @Mock
    private lateinit var clientConfig: ClientConfig

    @Mock
    private lateinit var model: SyftModel

    private lateinit var cut: JobDownloader

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        cut = JobDownloader()
    }
}