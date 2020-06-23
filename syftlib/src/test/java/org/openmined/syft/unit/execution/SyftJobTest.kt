package org.openmined.syft.unit.execution

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.openmined.syft.Syft
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.DownloadStatus
import org.openmined.syft.execution.JobDownloader
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.networking.requests.CommunicationAPI
import org.openmined.syft.proto.State
import org.openmined.syft.threading.ProcessSchedulers
import org.openmined.syftproto.execution.v1.StateOuterClass

private const val modelName = "myModel"
private const val modelVersion = "1.0"

@ExperimentalUnsignedTypes
internal class SyftJobTest {

    @Mock
    private lateinit var worker: Syft

    @Mock
    private lateinit var config: SyftConfiguration

    @Mock
    private lateinit var subscriber: JobStatusSubscriber

    @Mock
    private lateinit var jobDownloader: JobDownloader

    private lateinit var cut: SyftJob

    private val networkingSchedulers = object : ProcessSchedulers {
        override val computeThreadScheduler: Scheduler
            get() = Schedulers.trampoline()
        override val calleeThreadScheduler: Scheduler
            get() = Schedulers.trampoline()
    }
    private val computeSchedulers = object : ProcessSchedulers {
        override val computeThreadScheduler: Scheduler
            get() = Schedulers.trampoline()
        override val calleeThreadScheduler: Scheduler
            get() = Schedulers.trampoline()
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(config.computeSchedulers).doReturn(computeSchedulers)
        whenever(config.networkingSchedulers).doReturn(networkingSchedulers)

        cut = SyftJob(modelName, modelVersion, worker, config, jobDownloader)
    }

    @Test
    fun `Given a SyftJob when it starts then worker executes cycle`() {
        cut.start(subscriber)

        verify(worker).executeCycleRequest(cut)
    }

    @Test
    fun `Given a SyftJob that has been disposed when it starts then an exception is thrown`() {
        val parameterCaptor = ArgumentCaptor.forClass(IllegalThreadStateException::class.java)

        cut.dispose()

        cut.start(subscriber)

        verify(subscriber).onError(capture<IllegalThreadStateException>(parameterCaptor))
        assert(parameterCaptor.value is IllegalThreadStateException)
    }

    @Test
    fun `Given a SyftJob with a rejected cycle when it starts then nothing happens`() {
        cut.cycleRejected(CycleResponseData.CycleReject(modelName, modelVersion, "10"))

        verifyNoMoreInteractions(subscriber)
        verifyNoMoreInteractions(worker)
    }

    @Test
    fun `Given a SyftJob when download data is invoked it delegates it to job downloader`() {
        val responseData = mock<CycleResponseData.CycleAccept> {
            on { plans } doReturn HashMap()
            on { protocols } doReturn HashMap()
        }
        whenever(jobDownloader.status).doReturn(DownloadStatus.NOT_STARTED)

        cut.cycleAccepted(responseData)
        cut.downloadData("workerId", responseData)

        verify(jobDownloader).downloadData(
            workerId = eq("workerId"),
            config = any(),
            requestKey = anyOrNull(),
            networkDisposable = any(),
            jobStatusProcessor = any(),
            clientConfig = anyOrNull(),
            plans = any(),
            model = any(),
            protocols = any()
        )
    }

    @Test
    fun `Given a SyftJob when jobDownloader is running then another download is not run`() {
        val responseData = mock<CycleResponseData.CycleAccept> {
            on { plans } doReturn HashMap()
            on { protocols } doReturn HashMap()
        }
        cut.cycleAccepted(responseData)

        whenever(jobDownloader.status).doReturn(DownloadStatus.RUNNING)

        cut.downloadData("workerId", responseData)

        verify(jobDownloader).status
        verifyNoMoreInteractions((jobDownloader))
    }

    @Test
    fun `Given a SyftJob when try to download and cycle has not been accepted then an exception is thrown`() {
        val responseData = mock<CycleResponseData.CycleAccept>()
        cut.downloadData("workerId", responseData)

        verifyNoMoreInteractions((jobDownloader))
    }

    @Test
    fun `Given a SyftJob when it is disposed then its disposed status changes to true`() {
        cut.dispose()

        assertTrue(cut.isDisposed)
    }

    @Test
    fun `Given a SyftJob when cycle is accepted then response data is used to update state of job`() {
        val responseData = mock<CycleResponseData.CycleAccept> {
            on { plans } doReturn HashMap()
            on { protocols } doReturn HashMap()
        }
        cut.cycleAccepted(responseData)

        verify(responseData).requestKey
        verify(responseData).plans
        verify(responseData).protocols
        verify(responseData).modelId
        assert(cut.cycleStatus.get() == SyftJob.CycleStatus.ACCEPTED)
    }

    @Test
    fun `Given a SyftJob in accepted cycle when report is invoked then it is executed`() {
        val responseData = mock<CycleResponseData.CycleAccept> {
            on { requestKey } doReturn "requestKey"
        }

        cut.cycleAccepted(responseData)

        val signallingClient = mock<CommunicationAPI>()
        whenever(signallingClient.report(any())) doReturn Single.just(ReportResponse("any"))
        whenever(config.getSignallingClient()).doReturn(signallingClient)

        whenever(worker.getSyftWorkerId()).thenReturn("workerId")

        val diffState = mock<State>()
        val state = mock < StateOuterClass.State>()
        val diff = "diffString"
        whenever(diffState.serialize()).doReturn(state)
        whenever(state.toString()).doReturn(diff)

        cut.report(diffState)

        verify(config).getSignallingClient()
        verify(signallingClient).report(ReportRequest("workerId", "requestKey", diff))
    }

    @Test
    fun `Given a SyftJob when an error is thrown then the subscriber is notified and job is in disposed state`() {
        cut.start(subscriber)
        cut.throwError(TestException())

        verify(subscriber).onError(TestException())
        assertTrue(cut.isDisposed)
    }
}

private class TestException: Throwable() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}