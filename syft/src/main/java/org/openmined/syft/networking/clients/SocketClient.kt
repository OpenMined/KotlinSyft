package org.openmined.syft.networking.clients

import android.util.Log
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.json
import org.openmined.syft.networking.datamodels.NetworkModels
import org.openmined.syft.networking.datamodels.SocketResponse
import org.openmined.syft.networking.datamodels.syft.AuthenticationRequest
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.networking.datamodels.webRTC.InternalMessageRequest
import org.openmined.syft.networking.datamodels.webRTC.InternalMessageResponse
import org.openmined.syft.networking.datamodels.webRTC.JoinRoomRequest
import org.openmined.syft.networking.datamodels.webRTC.JoinRoomResponse
import org.openmined.syft.networking.requests.MessageTypes
import org.openmined.syft.networking.requests.NetworkingProtocol
import org.openmined.syft.networking.requests.REQUESTS
import org.openmined.syft.networking.requests.ResponseMessageTypes
import org.openmined.syft.networking.requests.SocketAPI
import org.openmined.syft.networking.requests.WebRTCMessageTypes
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SocketClient"

/**
 * Used to communicate and exchange data throw web socket with PyGrid
 * @property syftWebSocket Create web socket connection
 * @property timeout Timeout period
 * */
@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
internal class SocketClient(
    private val syftWebSocket: SyftWebSocket,
    private val timeout: UInt = 20000u
) : SocketAPI {

    constructor(baseUrl: String, timeout: UInt) :
            this(SyftWebSocket(NetworkingProtocol.WSS, baseUrl, timeout), timeout)

    //Choosing stable kotlin serialization over default
    private val Json = Json(JsonConfiguration.Stable)

    // Check to manage resource usage
    @Volatile
    private var socketClientSubscribed = AtomicBoolean(false)

    // Emit socket messages to subscribers
    private val messageFlow = MutableStateFlow<NetworkModels?>(null)

    /**
     * Authenticate socket connection with PyGrid
     * @see {@link org.openmined.syft.networking.datamodels.syft.AuthenticationRequest AuthenticationRequest}
     * */
    override suspend fun authenticate(authRequest: AuthenticationRequest): AuthenticationResponse {
        connectWebSocket()
        Log.d(
            TAG,
            "sending message: " + serializeNetworkModel(
                REQUESTS.AUTHENTICATION,
                authRequest
            ).toString()
        )
        syftWebSocket.send(serializeNetworkModel(REQUESTS.AUTHENTICATION, authRequest))
        return messageFlow.filterIsInstance<AuthenticationResponse>().first()
    }

    /**
     * Request or get current active federated learning cycle
     * @see {@link org.openmined.syft.networking.datamodels.syft.CycleRequest CycleRequest}
     * */
    override suspend fun getCycle(cycleRequest: CycleRequest): CycleResponseData {
        connectWebSocket()
        Log.d(
            TAG,
            "sending message: " + serializeNetworkModel(REQUESTS.CYCLE_REQUEST, cycleRequest)
        )
        syftWebSocket.send(serializeNetworkModel(REQUESTS.CYCLE_REQUEST, cycleRequest))
        return messageFlow.filterIsInstance<CycleResponseData>().first()
    }

    //todo handle backpressure and first or error
    /**
     * Report model state to PyGrid after the cycle completes
     * */
    override suspend fun report(reportRequest: ReportRequest): ReportResponse {
        connectWebSocket()
        syftWebSocket.send(serializeNetworkModel(REQUESTS.REPORT, reportRequest))
        // TODO Error management. Return null as error?
        return messageFlow.filterIsInstance<ReportResponse>().first()
    }

    //todo handle backpressure and first or error
    /**
     * Used by WebRTC to request PyGrid joining a FL cycle
     * */
    override suspend fun joinRoom(joinRoomRequest: JoinRoomRequest): JoinRoomResponse {
        connectWebSocket()
        syftWebSocket.send(
            serializeNetworkModel(
                WebRTCMessageTypes.WEBRTC_JOIN_ROOM,
                joinRoomRequest
            )
        )
        return messageFlow.filterIsInstance<JoinRoomResponse>().first()
    }

    //todo handle backpressure and first or error
    /**
     * Used by WebRTC connection to send message via PyGrid
     * */
    override suspend fun sendInternalMessage(internalMessageRequest: InternalMessageRequest): InternalMessageResponse {
        connectWebSocket()
        syftWebSocket.send(serializeNetworkModel(REQUESTS.WEBRTC_INTERNAL, internalMessageRequest))
        return messageFlow.filterIsInstance<InternalMessageResponse>().first()
    }

    /**
     * Listen and handle socket events
     * */
    private fun initiateNewWebSocket() {
        syftWebSocket.start()
                .onEach {
                    when (it) {
                        is NetworkMessage.SocketOpen -> {
                        }
                        is NetworkMessage.SocketError -> Log.e(
                            TAG,
                            "socket error",
                            it.throwable
                        )
                        is NetworkMessage.MessageReceived -> {
                            Log.d(TAG, "received the message " + it.message)
                            emitMessage(deserializeSocket(it.message))
                        }
                        else -> {
                        }
                    }
                    if (!socketClientSubscribed.get())
                        socketClientSubscribed.set(true)
                }
    }

    // Check socket status to free resources
    private fun isDisposed() = socketClientSubscribed.get()

    // Free resources
    fun dispose() {
        if (isDisposed()) {
            syftWebSocket.dispose()
            socketClientSubscribed.set(false)
            Log.d(TAG, "Socket Client Disposed")
        } else
            Log.d(TAG, "socket client already disposed")
    }

    /**
     * Prevent initializing new socket connection if any exists
     * */
    private fun connectWebSocket() {
        if (socketClientSubscribed.get())
            return
        initiateNewWebSocket()
    }

    /**
     * Emit message to the subscribers
     * */
    private fun emitMessage(response: SocketResponse) {
        messageFlow.value = response.data
    }

    /**
     * Parse incoming message
     * */
    private fun deserializeSocket(socketMessage: String): SocketResponse {
        return Json.parse(SocketResponse.serializer(), socketMessage)
    }

    /**
     * Serialize message to be sent to PyGrid
     * */
    private fun serializeNetworkModel(types: MessageTypes, data: NetworkModels) = json {
        TYPE to types.value
        if (types is ResponseMessageTypes)
            DATA to types.serialize(data)
        else
        //todo change this appropriately when needed
            DATA to data.toString()
    }
}