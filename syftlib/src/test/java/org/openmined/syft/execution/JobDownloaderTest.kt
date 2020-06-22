package org.openmined.syft.execution

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.requests.HttpAPI
import org.openmined.syft.proto.SyftModel
import java.util.concurrent.ConcurrentHashMap

@ExperimentalUnsignedTypes
class JobDownloaderTest {

    @Mock
    private lateinit var configuration: SyftConfiguration

    @Mock
    private lateinit var clientConfig: ClientConfig

    @Mock
    private lateinit var model: SyftModel

    @Mock
    private lateinit var httpAPI: HttpAPI

    private lateinit var cut: JobDownloader

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(configuration.getDownloader()) doReturn httpAPI
        whenever(configuration.filesDir) doReturn mock()

        cut = JobDownloader()
    }

    @Test(expected = IllegalStateException::class)
    fun `Given modelId has not been set then an exception is thrown`() {
        val workerId = "workerId"
        val request = "request"
        val plans = ConcurrentHashMap<String, Plan>()
        val protocols = ConcurrentHashMap<String, Protocol>()

        cut.getDownloadables(workerId, configuration, request, model, plans, protocols)
    }
}