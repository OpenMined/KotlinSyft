package org.openmined.syft.unit.networking.clients

//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import io.mockk.MockKAnnotations
//import io.mockk.coEvery
//import io.mockk.every
//import io.mockk.impl.annotations.MockK
//import io.mockk.mockk
//import io.mockk.verify
//import io.reactivex.Scheduler
//import io.reactivex.schedulers.Schedulers
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.test.TestCoroutineDispatcher
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runBlockingTest
//import kotlinx.coroutines.test.setMain
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.json.json
//import org.junit.After
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.openmined.syft.networking.clients.DATA
//import org.openmined.syft.networking.clients.NetworkMessage
//import org.openmined.syft.networking.clients.SocketClient
//import org.openmined.syft.networking.clients.SyftWebSocket
//import org.openmined.syft.networking.clients.TYPE
//import org.openmined.syft.networking.datamodels.syft.AuthenticationRequest
//import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
//import org.openmined.syft.networking.datamodels.syft.CYCLE_ACCEPT
//import org.openmined.syft.networking.datamodels.syft.CycleRequest
//import org.openmined.syft.networking.datamodels.syft.CycleResponseData
//import org.openmined.syft.networking.requests.REQUESTS
//import org.openmined.syft.threading.ProcessSchedulers
//import java.util.concurrent.TimeUnit

/**
 * This class tests the Socket client for FL messages
 * The serialization and deserialization of socket messages is also verified by default
 */
//@ExperimentalCoroutinesApi
//@ExperimentalUnsignedTypes
internal class SocketClientTest {

//    @MockK
//    private lateinit var webSocket: SyftWebSocket
//
//    @get:Rule
//    val instantExecutorRule = InstantTaskExecutorRule()
//    private val testDispatcher = TestCoroutineDispatcher()
//
//    @After
//    fun cleanUp() {
//        Dispatchers.resetMain()
//        testDispatcher.cleanupTestCoroutines()
//    }
//
//    @Before
//    fun setUp() {
//        MockKAnnotations.init(this)
//        Dispatchers.setMain(testDispatcher)
//    }
//
//    @Test
//    fun `verify socket is initialised only once when empty on authenticate`() = runBlocking {
//        kotlinx.coroutines.withContext(Dispatchers.Main) {
//            val authenticationRequest = AuthenticationRequest("test", "model name", "1.0")
//
//            val message = "hello"
//            coEvery { webSocket.start() } returns MutableStateFlow<NetworkMessage>(NetworkMessage.MessageReceived(
//                message))
//            every { webSocket.send(any()) } returns true
//
//            val cut = SocketClient(webSocket)
//
//            runBlockingTest {
//                cut.authenticate(authenticationRequest)
//            }
//
//        cut.authenticate(authenticationRequest)
//
//            verify(exactly = 1) { webSocket.start() }
//        }
//    }
//
//    @Test
//    fun `verify authentication success response on socket client authenticate`() {
//        val authenticationRequest = AuthenticationRequest("test","model name")
//        val serializedAuthRequest = json {
//            TYPE to REQUESTS.AUTHENTICATION.value
//            DATA to REQUESTS.AUTHENTICATION.serialize(authenticationRequest)
//        }
//        val authenticationResponse = json {
//            TYPE to REQUESTS.AUTHENTICATION.value
//            DATA to json {
//                "status" to "success"
//                "worker_id" to "test_id"
//                "requires_speed_test" to true
//            }
//        }
//        webSocket.stub {
//            on { send(serializedAuthRequest) }.thenReturn(true)
//        }
//
//        val testAuthenticate = socketClient.authenticate(authenticationRequest)
//                .subscribeOn(testScheduler)
//                .observeOn(testScheduler)
//                .test()
//        verify(webSocket).send(serializedAuthRequest)
//        testScheduler.advanceTimeBy(1L, TimeUnit.SECONDS)
//        processor.offer(NetworkMessage.MessageReceived(authenticationResponse.toString()))
//        testScheduler.advanceTimeBy(1L, TimeUnit.SECONDS)
//        testAuthenticate.assertValue(AuthenticationResponse.AuthenticationSuccess("test_id", true))
//        testAuthenticate.dispose()
//    }
//
//    @Test
//    fun `verify cycle accepted response on socket client getCycle`() {
//        val cycleRequest = CycleRequest(
//            "auth",
//            "model name",
//            "1",
//            10,
//            1000.0f,
//            1000.0f
//        )
//        val serializedRequest = json {
//            TYPE to REQUESTS.CYCLE_REQUEST.value
//            DATA to REQUESTS.CYCLE_REQUEST.serialize(cycleRequest)
//        }
//        val cycleResponseSerialized = json {
//            "status" to CYCLE_ACCEPT
//            "model" to "model name"
//            "version" to "1"
//            "request_key" to "random key"
//            "plans" to json {
//                "plan name" to "plan id"
//            }
//            "client_config" to json {
//                "name" to "model name"
//                "version" to "1"
//                "batch_size" to 1L
//                "lr" to 0.1f
//                "max_updates" to 1
//            }
//            "protocols" to json {}
//            "model_id" to "model id"
//        }
//        val socketResponse = json {
//            TYPE to REQUESTS.CYCLE_REQUEST.value
//            DATA to cycleResponseSerialized
//        }
//
//        val cycleResponse = REQUESTS.CYCLE_REQUEST.parseJson(
//            cycleResponseSerialized.toString()
//        ) as CycleResponseData.CycleAccept
//
//        webSocket.stub {
//            on { send(serializedRequest) }.thenReturn(true)
//        }
//        //begin websocket listener
//        socketClient.initiateNewWebSocket()
//
//        val testCycle = socketClient.getCycle(cycleRequest)
//                .subscribeOn(testScheduler)
//                .observeOn(testScheduler)
//                .test()
//        verify(webSocket).send(serializedRequest)
//        testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
//        processor.offer(NetworkMessage.MessageReceived(socketResponse.toString()))
//        testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
//        testCycle.assertValue(cycleResponse)
//        testCycle.dispose()
//    }

// TODO("enable this test when webrtc is functional")
//
//    @Test
//    fun `verify internal message response on socket client sendInternalMessage`() {
//        val internalRequest = InternalMessageRequest(
//            "worker id",
//            "scope id",
//            "target",
//            "type",
//            "message"
//        )
//        val serializedRequest = json {
//            TYPE to REQUESTS.WEBRTC_INTERNAL.value
//            DATA to REQUESTS.WEBRTC_INTERNAL.serialize(internalRequest)
//        }
//        val internalWebRTCResponseSerialized = json {
//            TYPE to REQUESTS.WEBRTC_INTERNAL.value
//            DATA to json {
//                "type" to "offer"
//                "worker_id" to "test_id"
//                "sdp_string" to "sdp"
//            }
//        }
//
//        val internalMessageResponse = REQUESTS.WEBRTC_INTERNAL.parseJson(
//            internalWebRTCResponseSerialized[DATA].toString()
//        ) as InternalMessageResponse
//
//        webSocket.stub {
//            on { send(serializedRequest) }.thenReturn(true)
//            on { start() }.thenReturn(processor)
//        }
//        socketClient.authenticate(authenticationRequest)
//        val testSendInternalMSG = socketClient.sendInternalMessage(internalRequest)
//                .subscribeOn(testScheduler)
//                .observeOn(testScheduler)
//                .test()
//        verify(webSocket).send(serializedRequest)
//        processor.offer(NetworkMessage.MessageReceived(internalWebRTCResponseSerialized.toString()))
//        testScheduler.advanceTimeBy(1L, TimeUnit.SECONDS)
//        testSendInternalMSG.assertValue(internalMessageResponse)
//        testSendInternalMSG.dispose()
//    }

}