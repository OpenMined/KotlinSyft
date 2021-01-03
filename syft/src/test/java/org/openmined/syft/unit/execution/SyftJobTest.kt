package org.openmined.syft.unit.execution

import android.util.Base64
import android.util.Base64.encodeToString
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.openmined.syft.Syft
import org.openmined.syft.domain.DownloadStatus
import org.openmined.syft.domain.JobRepository
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobErrorThrowable
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.networking.requests.CommunicationAPI
import org.openmined.syft.proto.SyftState
import org.openmined.syft.threading.ProcessSchedulers
import org.openmined.syftproto.execution.v1.StateOuterClass
import org.robolectric.RobolectricTestRunner
import java.io.File


private const val modelName = "myModel"
private const val modelVersion = "1.0"

@ExperimentalUnsignedTypes
class SyftJobTest {

//    @Mock
//    private lateinit var worker: Syft
//
//    @Mock
//    private lateinit var config: SyftConfiguration
//
//    @Mock
//    private lateinit var subscriber: JobStatusSubscriber
//
//    @Mock
//    private lateinit var jobRepository: JobRepository
//
//    @Mock
//    private lateinit var stateMock: SyftState
//
//    private lateinit var cut: SyftJob
//
//    private val networkingSchedulers = object : ProcessSchedulers {
//        override val computeThreadScheduler: Scheduler
//            get() = Schedulers.trampoline()
//        override val calleeThreadScheduler: Scheduler
//            get() = Schedulers.trampoline()
//    }
//    private val computeSchedulers = object : ProcessSchedulers {
//        override val computeThreadScheduler: Scheduler
//            get() = Schedulers.trampoline()
//        override val calleeThreadScheduler: Scheduler
//            get() = Schedulers.trampoline()
//    }
//
//    @Before
//    fun setUp() {
//        MockitoAnnotations.initMocks(this)
//        mockkObject(SyftState.Companion)
//        every { SyftState.loadSyftState(any()) } returns mockk()
//        whenever(config.computeSchedulers).doReturn(computeSchedulers)
//        whenever(config.networkingSchedulers).doReturn(networkingSchedulers)
//        cut = SyftJob(modelName, modelVersion, worker, config, jobRepository)
//    }
//
//    @After
//    fun clear() {
//        unmockkAll()
//    }
//
//    @Test
//    fun `Given a SyftJob when it starts then worker executes cycle`() {
//        cut.start(subscriber)
//
//        verify(worker).executeCycleRequest(cut)
//    }
//
//    @Test
//    fun `Given a SyftJob that has been disposed when it starts then an exception is thrown`() {
//        val parameterCaptor = ArgumentCaptor.forClass(JobErrorThrowable::class.java)
//
//        cut.dispose()
//
//        cut.start(subscriber)
//
//        verify(subscriber).onError(capture<JobErrorThrowable>(parameterCaptor))
//        assert(parameterCaptor.value is JobErrorThrowable.RunningDisposedJob)
//    }
//
//    @Test
//    fun `Given a SyftJob with a rejected cycle when it starts then nothing happens`() {
//        cut.cycleRejected(CycleResponseData.CycleReject("10"))
//
//        verifyNoMoreInteractions(subscriber)
//        verifyNoMoreInteractions(worker)
//    }
//
//    @Test
//    fun `Given a SyftJob when download data is invoked it delegates it to job downloader`() {
//        val responseData = mock<CycleResponseData.CycleAccept> {
//            on { plans } doReturn HashMap()
//            on { protocols } doReturn HashMap()
//        }
//        whenever(jobRepository.status).doReturn(DownloadStatus.NOT_STARTED)
//
//        cut.cycleAccepted(responseData)
//        cut.downloadData("workerId", responseData)
//
//        verify(jobRepository).downloadData(
//            workerId = eq("workerId"),
//            config = any(),
//            requestKey = anyOrNull(),
//            networkDisposable = any(),
//            jobStatusProcessor = any(),
//            clientConfig = anyOrNull(),
//            plans = any(),
//            model = any(),
//            protocols = any()
//        )
//    }
//
//    @Test
//    fun `createDiff creates the diff module and then evaluates diff`() {
//        whenever(stateMock.createDiff(any(), any())).doReturn(mockk())
//
//        val config = mockk<SyftConfiguration>() {
//            every { filesDir } returns File("test")
//        }
//        whenever(jobRepository.getDiffScript(config)).doReturn(mockk())
//        whenever(
//            jobRepository.persistToLocalStorage(
//                any(),
//                any(),
//                any(),
//                eq(false)
//            )
//        ).doReturn("test module path")
//
//        val jobTest = SyftJob(modelName, modelVersion, worker, config, jobRepository)
//
//        jobTest.model.modelSyftState = stateMock
//        jobTest.createDiff()
//        verify(jobRepository).persistToLocalStorage(any(), any(), any(), eq(false))
//        verify(stateMock).createDiff(any(), eq("test module path"))
//    }
//
//    @Test
//    fun `Given a SyftJob when jobDownloader is running then another download is not run`() {
//        val responseData = mock<CycleResponseData.CycleAccept> {
//            on { plans } doReturn HashMap()
//            on { protocols } doReturn HashMap()
//        }
//        cut.cycleAccepted(responseData)
//
//        whenever(jobRepository.status).doReturn(DownloadStatus.RUNNING)
//
//        cut.downloadData("workerId", responseData)
//
//        verify(jobRepository).status
//        verifyNoMoreInteractions((jobRepository))
//    }
//
//    @Test
//    fun `Given a SyftJob when try to download and cycle has not been accepted then an exception is thrown`() {
//        val responseData = mock<CycleResponseData.CycleAccept>()
//        cut.downloadData("workerId", responseData)
//
//        verifyNoMoreInteractions((jobRepository))
//    }
//
//    @Test
//    fun `Given a SyftJob when it is disposed then its disposed status changes to true`() {
//        cut.dispose()
//
//        assertTrue(cut.isDisposed)
//    }
//
//    @Test
//    fun `Given a SyftJob when cycle is accepted then response data is used to update state of job`() {
//        val responseData = mock<CycleResponseData.CycleAccept> {
//            on { plans } doReturn HashMap()
//            on { protocols } doReturn HashMap()
//        }
//        cut.cycleAccepted(responseData)
//
//        verify(responseData).requestKey
//        verify(responseData).plans
//        verify(responseData).protocols
//        verify(responseData).modelId
//        assert(cut.cycleStatus.get() == SyftJob.CycleStatus.ACCEPTED)
//    }
//
//    @Test
//    fun `Given a SyftJob when an error is thrown then the subscriber is notified and job is in disposed state`() {
//        val exception = JobErrorThrowable.ExternalException(
//            TestException().message,
//            TestException().cause
//        )
//        cut.start(subscriber)
//        cut.publishError(exception)
//
//        verify(subscriber).onError(exception)
//        assertTrue(cut.isDisposed)
//    }
//}
//
//@ExperimentalUnsignedTypes
//@RunWith(RobolectricTestRunner::class)
//internal class RobolectricJobTests {
//
//    @Mock
//    private lateinit var worker: Syft
//
//    @Mock
//    private lateinit var config: SyftConfiguration
//
//    @Mock
//    private lateinit var subscriber: JobStatusSubscriber
//
//    @Mock
//    private lateinit var jobRepository: JobRepository
//
//    private lateinit var cut: SyftJob
//
//    private val networkingSchedulers = object : ProcessSchedulers {
//        override val computeThreadScheduler: Scheduler
//            get() = Schedulers.trampoline()
//        override val calleeThreadScheduler: Scheduler
//            get() = Schedulers.trampoline()
//    }
//    private val computeSchedulers = object : ProcessSchedulers {
//        override val computeThreadScheduler: Scheduler
//            get() = Schedulers.trampoline()
//        override val calleeThreadScheduler: Scheduler
//            get() = Schedulers.trampoline()
//    }
//
//    @Before
//    fun setUp() {
//        MockitoAnnotations.initMocks(this)
//
//        whenever(config.computeSchedulers).doReturn(computeSchedulers)
//        whenever(config.networkingSchedulers).doReturn(networkingSchedulers)
//
//        cut = SyftJob(modelName, modelVersion, worker, config, jobRepository)
//    }
//
//    @Test
//    fun `Given a SyftJob in accepted cycle when report is invoked then it is executed`() {
//        val responseData = mock<CycleResponseData.CycleAccept> {
//            on { requestKey } doReturn "requestKey"
//        }
//
//        cut.cycleAccepted(responseData)
//
//        val signallingClient = mock<CommunicationAPI>()
//        whenever(signallingClient.report(any())) doReturn Single.just(ReportResponse("any"))
//        whenever(config.getSignallingClient()).doReturn(signallingClient)
//
//        whenever(worker.getSyftWorkerId()).thenReturn("workerId")
//        whenever(worker.isBatteryValid()).thenReturn(true)
//        whenever(worker.isNetworkValid()).thenReturn(true)
//        val diffState = mock<SyftState>()
//        val state = mock<StateOuterClass.State>()
//        val diff = "hello".toByteArray()
//        whenever(diffState.serialize()).doReturn(state)
//        whenever(state.toByteArray()).doReturn(diff)
//        cut.report(diffState)
//
//        verify(config).getSignallingClient()
//        verify(signallingClient).report(
//            ReportRequest(
//                "workerId",
//                "requestKey",
//                encodeToString(diff, Base64.DEFAULT)
//            )
//        )
//    }
}

private class TestException : Throwable() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}