package org.openmined.syft.execution

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

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

    @Test
    fun `Given jobDownloader when data is downloaded and had already started then function returns and no other objects are invoked`() {
        val workerId = "workerId"
        val trainingParamsStatus = mock<AtomicReference<SyftJob.DownloadStatus>> {
            on { get() } doReturn SyftJob.DownloadStatus.RUNNING
        }
        val networkDisposable = mock<CompositeDisposable>()

        cut.downloadData(
            workerId,
            configuration,
            null,
            networkDisposable,
            PublishProcessor.create(),
            clientConfig,
            ConcurrentHashMap<String, Plan>(),
            model,
            ConcurrentHashMap<String, Protocol>(),
            trainingParamsStatus
        )

        verifyNoMoreInteractions(networkDisposable)
    }

    @Test(expected = IllegalStateException::class)
    fun `Given jobDownloader when data is downloaded and requestKey is null then throws an exception`() {
        val workerId = "workerId"
        val networkDisposable = mock<CompositeDisposable>()

        cut.downloadData(
            workerId,
            configuration,
            null,
            networkDisposable,
            PublishProcessor.create(),
            clientConfig,
            ConcurrentHashMap<String, Plan>(),
            model,
            ConcurrentHashMap<String, Protocol>()
        )
    }
}