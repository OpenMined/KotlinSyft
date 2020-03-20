package org.openmined.syft.networking.clients

import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.json
import org.openmined.syft.networking.datamodels.NetworkModels
import org.openmined.syft.networking.datamodels.SocketResponse
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
import org.openmined.syft.networking.requests.Protocol
import org.openmined.syft.networking.requests.REQUESTS
import org.openmined.syft.networking.requests.ResponseMessageTypes
import org.openmined.syft.networking.requests.SocketAPI
import org.openmined.syft.networking.requests.WebRTCMessageTypes
import org.openmined.syft.processes.SyftJob
import org.openmined.syft.threading.ProcessSchedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SocketClient"

@ExperimentalUnsignedTypes
class SocketClient(
    baseUrl: String,
    private val timeout: UInt = 20000u,
    private val schedulers: ProcessSchedulers
) : SocketAPI {

    //Choosing stable kotlin serialization over default
    private val Json = Json(JsonConfiguration.Stable)

    private val syftWebSocket = SyftWebSocket(Protocol.WSS, baseUrl, timeout)
    @Volatile
    private var socketClientSubscribed = AtomicBoolean(false)
    private val messageProcessor = PublishProcessor.create<NetworkModels>()
    private val compositeDisposable = CompositeDisposable()

    override fun authenticate(): Single<AuthenticationResponse> {
        initiateSocketIfEmpty()
        Log.d(TAG, "sending message: " + serializeNetworkModel(REQUESTS.AUTHENTICATION).toString())
        syftWebSocket.send(serializeNetworkModel(REQUESTS.AUTHENTICATION))
        return messageProcessor.onBackpressureLatest()
                .ofType(AuthenticationResponse::class.java)
                .firstOrError()
    }

    override fun getCycle(cycleRequest: CycleRequest): Single<CycleResponseData> {
        Log.d(TAG, "sending message: " + serializeNetworkModel(REQUESTS.CYCLE_REQUEST, cycleRequest))
        syftWebSocket.send(serializeNetworkModel(REQUESTS.CYCLE_REQUEST, cycleRequest))
        return messageProcessor.onBackpressureBuffer()
                .ofType(CycleResponseData::class.java)
                .filter {
                    SyftJob.JobID(
                        cycleRequest.modelName,
                        cycleRequest.version
                    ).matchWithResponse(it.modelName)
                }.debounce(timeout.toLong(), TimeUnit.MILLISECONDS)
                .firstOrError()
    }

    //todo handle backpressure and first or error
    override fun report(reportRequest: ReportRequest): Single<ReportResponse> {
        syftWebSocket.send(serializeNetworkModel(REQUESTS.REPORT, reportRequest))
        return messageProcessor.onBackpressureDrop()
                .ofType(ReportResponse::class.java)
                .firstOrError()
    }

    //todo handle backpressure and first or error
    override fun joinRoom(joinRoomRequest: JoinRoomRequest): Single<JoinRoomResponse> {
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
        syftWebSocket.send(serializeNetworkModel(REQUESTS.WEBRTC_INTERNAL, internalMessageRequest))
        return messageProcessor.onBackpressureBuffer()
                .ofType(InternalMessageResponse::class.java)
                .first(null)
    }

    private fun initiateSocketIfEmpty() {
        if (socketClientSubscribed.get())
            return
        compositeDisposable.add(syftWebSocket.start()
                .map {
                    when (it) {
                        is NetworkMessage.SocketOpen -> authenticate()
                        is NetworkMessage.SocketError -> Log.e(
                            TAG,
                            "socket error",
                            it.throwable
                        )
                        is NetworkMessage.MessageReceived -> {
                            Log.d(TAG,"received the message "+it.message)
                            emitMessage(deserializeSocket(it.message))
                        }
                    }
                }
                .subscribeOn(schedulers.computeThreadScheduler)
                .observeOn(schedulers.calleeThreadScheduler)
                .subscribe())
        socketClientSubscribed.set(true)
    }

    private fun emitMessage(response: SocketResponse) {
        messageProcessor.offer(response.data)
    }

    private fun deserializeSocket(socketMessage: String): SocketResponse {
        return Json.parse(SocketResponse.serializer(), socketMessage)
    }

    private fun serializeNetworkModel(types: MessageTypes, data: NetworkModels? = null) = json {
        TYPE to types.value
        if (data != null) {
            if (types is ResponseMessageTypes)
                DATA to types.serialize(data)
            else
            //todo change this appropriately when needed
                DATA to data.toString()
        }
    }
}