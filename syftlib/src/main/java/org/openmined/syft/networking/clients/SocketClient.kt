package org.openmined.syft.networking.clients

import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.json
import org.openmined.syft.execution.SyftJob
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
import org.openmined.syft.threading.ProcessSchedulers
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SocketClient"

@ExperimentalUnsignedTypes
class SocketClient(
    private val syftWebSocket: SyftWebSocket,
    private val timeout: UInt = 20000u,
    private val schedulers: ProcessSchedulers
) : SocketAPI, Disposable {

    constructor(baseUrl: String, timeout: UInt, schedulers: ProcessSchedulers) :
            this(SyftWebSocket(NetworkingProtocol.WSS, baseUrl, timeout), timeout, schedulers)

    //Choosing stable kotlin serialization over default
    private val Json = Json(JsonConfiguration.Stable)

    @Volatile
    private var socketClientSubscribed = AtomicBoolean(false)
    private val messageProcessor = PublishProcessor.create<NetworkModels>()
    private val compositeDisposable = CompositeDisposable()

    override fun authenticate(authRequest: AuthenticationRequest): Single<AuthenticationResponse> {
        connectWebSocket()
        Log.d(
            TAG,
            "sending message: " + serializeNetworkModel(
                REQUESTS.AUTHENTICATION,
                authRequest
            ).toString()
        )
        syftWebSocket.send(serializeNetworkModel(REQUESTS.AUTHENTICATION, authRequest))
        return messageProcessor.onBackpressureLatest()
                .ofType(AuthenticationResponse::class.java)
                .firstOrError()
    }

    override fun getCycle(cycleRequest: CycleRequest): Single<CycleResponseData> {
        connectWebSocket()
        Log.d(
            TAG,
            "sending message: " + serializeNetworkModel(REQUESTS.CYCLE_REQUEST, cycleRequest)
        )
        syftWebSocket.send(serializeNetworkModel(REQUESTS.CYCLE_REQUEST, cycleRequest))
        return messageProcessor.onBackpressureBuffer()
                .ofType(CycleResponseData::class.java)
                .filter {
                    SyftJob.JobID(
                        cycleRequest.modelName,
                        cycleRequest.version
                        //todo when pygrid updates to have version in rejected
                        // we need to supply version here as well
                    ).matchWithResponse(it.modelName)
                }
                .firstOrError()
    }

    //todo handle backpressure and first or error
    override fun report(reportRequest: ReportRequest): Single<ReportResponse> {
        connectWebSocket()
        syftWebSocket.send(serializeNetworkModel(REQUESTS.REPORT, reportRequest))
        return messageProcessor.onBackpressureDrop()
                .ofType(ReportResponse::class.java)
                .firstOrError()
    }

    //todo handle backpressure and first or error
    override fun joinRoom(joinRoomRequest: JoinRoomRequest): Single<JoinRoomResponse> {
        connectWebSocket()
        syftWebSocket.send(
            serializeNetworkModel(
                WebRTCMessageTypes.WEBRTC_JOIN_ROOM,
                joinRoomRequest
            )
        )
        return messageProcessor.onBackpressureBuffer()
                .ofType(JoinRoomResponse::class.java)
                .firstOrError()
    }

    //todo handle backpressure and first or error
    override fun sendInternalMessage(internalMessageRequest: InternalMessageRequest): Single<InternalMessageResponse> {
        connectWebSocket()
        syftWebSocket.send(serializeNetworkModel(REQUESTS.WEBRTC_INTERNAL, internalMessageRequest))
        return messageProcessor.onBackpressureBuffer()
                .ofType(InternalMessageResponse::class.java)
                .first(null)
    }

    fun initiateNewWebSocket() {
        compositeDisposable.add(syftWebSocket.start()
                .map {
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
                    }
                }
                .subscribeOn(schedulers.computeThreadScheduler)
                .observeOn(schedulers.calleeThreadScheduler)
                .doOnEach {
                    if (!socketClientSubscribed.get())
                        socketClientSubscribed.set(true)
                }
                .subscribe()
        )
    }

    override fun isDisposed() = socketClientSubscribed.get()

    override fun dispose() {
        syftWebSocket.close()
        compositeDisposable.clear()
        socketClientSubscribed.set(false)
    }

    private fun connectWebSocket() {
        if (socketClientSubscribed.get())
            return
        initiateNewWebSocket()
    }

    private fun emitMessage(response: SocketResponse) {
        messageProcessor.offer(response.data)
    }

    private fun deserializeSocket(socketMessage: String): SocketResponse {
        return Json.parse(SocketResponse.serializer(), socketMessage)
    }

    private fun serializeNetworkModel(types: MessageTypes, data: NetworkModels) = json {
        TYPE to types.value
            if (types is ResponseMessageTypes)
                DATA to types.serialize(data)
            else
            //todo change this appropriately when needed
                DATA to data.toString()
        }
}