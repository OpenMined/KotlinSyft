package org.openmined.syft.execution

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.openmined.syft.datasource.LocalDataSource
import org.openmined.syft.datasource.RemoteDataSource
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.requests.HttpAPI
import org.openmined.syft.proto.SyftModel
import org.openmined.syft.threading.ProcessSchedulers
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

@ExperimentalUnsignedTypes
class JobDownloaderTest {

    @Mock
    private lateinit var config: SyftConfiguration

    @Mock
    private lateinit var clientConfig: ClientConfig

    @Mock
    private lateinit var model: SyftModel

    @Mock
    private lateinit var httpAPI: HttpAPI

    @Mock
    private lateinit var localDataSource: LocalDataSource

    @Mock
    private lateinit var remoteDataSource: RemoteDataSource

    private lateinit var cut: JobDownloader

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

        whenever(config.getDownloader()) doReturn httpAPI
        whenever(config.filesDir) doReturn File("/filesDir")

        cut = JobDownloader(localDataSource, remoteDataSource)
    }

    @Test
    fun `Given a model is provided when list of downloadables is requested then model is downloaded, saved locally and loaded`() {
        val workerId = "workerId"
        val requestKey = "request"
        val plans = ConcurrentHashMap<String, Plan>()
        val protocols = ConcurrentHashMap<String, Protocol>()
        val modelPath = "/modelPath"
        val modelId = "modelId"
        val modelIS: InputStream = "Model Content".byteInputStream()
        val networkDisposable = CompositeDisposable()
        val jobStatusProcessor = PublishProcessor.create<JobStatusMessage>()

        whenever(model.pyGridModelId) doReturn modelId

        whenever(
            remoteDataSource.downloadModel(workerId, requestKey, modelId)
        ) doReturn Single.just(modelIS)
        whenever(
            localDataSource.save(modelIS, "${config.filesDir}/models", "$modelId.pb")
        ) doReturn Single.just(modelPath)

        cut.downloadData(
            workerId,
            config,
            requestKey,
            networkDisposable,
            jobStatusProcessor,
            clientConfig,
            plans,
            model,
            protocols
        )

        verify(remoteDataSource).downloadModel(workerId, requestKey, modelId)
        verify(localDataSource).save(modelIS, "${config.filesDir}/models", "$modelId.pb")
        verify(model).loadModelState(modelPath)
    }

    @Test
    fun `Given a list of protocols and a model when list of downloadables is requested then protocols are downloaded and saved locally`() {
        val workerId = "workerId"
        val requestKey = "request"
        val plans = ConcurrentHashMap<String, Plan>()
        val protocolId = "p1"
        val protocol = Protocol(protocolId).also {
            it.protocolFileLocation = "somewhereOverTheRainbow"
        }
        val protocols = ConcurrentHashMap<String, Protocol>().also {
            it["p1"] = protocol
        }
        val protocolPath = "/somewhere/protocols/p1.pb"
        val modelPath = "/modelPath"
        val modelId = "modelId"
        val modelIS: InputStream = "Model Content".byteInputStream()
        val protocolIS: InputStream = "Protocol Content".byteInputStream()
        val networkDisposable = CompositeDisposable()
        val jobStatusProcessor = PublishProcessor.create<JobStatusMessage>()

        whenever(model.pyGridModelId) doReturn modelId
        whenever(
            remoteDataSource.downloadModel(workerId, requestKey, modelId)
        ) doReturn Single.just(modelIS)
        whenever(
            localDataSource.save(modelIS, "${config.filesDir}/models", "$modelId.pb")
        ) doReturn Single.just(modelPath)


        whenever(
            remoteDataSource.downloadProtocol(eq(workerId), eq(requestKey), any())
        ) doReturn Single.just(protocolIS)
        whenever(
            localDataSource.save(protocolIS, "${config.filesDir}/protocols", "$protocolId.pb")
        ) doReturn Single.just(protocolPath)

        cut.downloadData(
            workerId,
            config,
            requestKey,
            networkDisposable,
            jobStatusProcessor,
            clientConfig,
            plans,
            model,
            protocols
        )

        verify(remoteDataSource).downloadModel(workerId, requestKey, modelId)
        verify(remoteDataSource).downloadProtocol(workerId, requestKey, protocolId)
        verify(localDataSource).save(modelIS, "${config.filesDir}/models", "$modelId.pb")
        verify(model).loadModelState(modelPath)
        verify(localDataSource).save(protocolIS, "${config.filesDir}/protocols", "$protocolId.pb")
    }

    @Test
    fun `Given a list of plans and a model when list of downloadables is requested then protocols are downloaded and saved locally`() {
        val workerId = "workerId"
        val requestKey = "request"
        val planId = "p1"
        val plan = Plan(mock(), planId)
        val plans = ConcurrentHashMap<String, Plan>().also {
            it["p1"] = plan
        }
        val protocols = ConcurrentHashMap<String, Protocol>()
        val protocolPath = "/somewhere/protocols/p1.pb"
        val modelPath = "/modelPath"
        val modelId = "modelId"
        val modelIS: InputStream = "Model Content".byteInputStream()
        val planIS: InputStream = "Plan Content".byteInputStream()
        val networkDisposable = CompositeDisposable()
        val jobStatusProcessor = PublishProcessor.create<JobStatusMessage>()

        whenever(model.pyGridModelId) doReturn modelId
        whenever(
            remoteDataSource.downloadModel(workerId, requestKey, modelId)
        ) doReturn Single.just(modelIS)
        whenever(
            localDataSource.save(modelIS, "${config.filesDir}/models", "$modelId.pb")
        ) doReturn Single.just(modelPath)


        whenever(
            remoteDataSource.downloadPlan(eq(workerId), eq(requestKey), eq(planId), eq(PLAN_OP_TYPE))
        ) doReturn Single.just(planIS)
        whenever(
            localDataSource.save(planIS, "${config.filesDir}/plans", "$planId.pb")
        ) doReturn Single.just(protocolPath)

        cut.downloadData(
            workerId,
            config,
            requestKey,
            networkDisposable,
            jobStatusProcessor,
            clientConfig,
            plans,
            model,
            protocols
        )

        verify(remoteDataSource).downloadModel(workerId, requestKey, modelId)
        verify(remoteDataSource).downloadPlan(workerId, requestKey, planId, PLAN_OP_TYPE)
        verify(localDataSource).save(planIS, "${config.filesDir}/plans", "$planId.pb")
    }

    @Test(expected = IllegalStateException::class)
    fun `Given modelId has not been set then an exception is thrown`() {
        val workerId = "workerId"
        val requestKey = "request"
        val plans = ConcurrentHashMap<String, Plan>()
        val protocols = ConcurrentHashMap<String, Protocol>()
        val networkDisposable = CompositeDisposable()
        val jobStatusProcessor = PublishProcessor.create<JobStatusMessage>()

        cut.downloadData(
            workerId,
            config,
            requestKey,
            networkDisposable,
            jobStatusProcessor,
            clientConfig,
            plans,
            model,
            protocols
        )
    }
}