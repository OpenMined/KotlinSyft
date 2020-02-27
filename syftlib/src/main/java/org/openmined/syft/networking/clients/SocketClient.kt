package org.openmined.syft.networking.clients

import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.json
import org.openmined.syft.processes.SyftJob
import org.openmined.syft.networking.datamodels.NetworkModels
import org.openmined.syft.networking.datamodels.SocketResponse
import org.openmined.syft.networking.datamodels.webRTC.InternalMessageRequest
import org.openmined.syft.networking.datamodels.webRTC.InternalMessageResponse
import org.openmined.syft.networking.datamodels.webRTC.JoinRoomRequest
import org.openmined.syft.networking.datamodels.webRTC.JoinRoomResponse
import org.openmined.syft.networking.datamodels.syft.AuthenticationSuccess
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.networking.requests.MessageTypes
import org.openmined.syft.networking.requests.Protocol
import org.openmined.syft.networking.requests.REQUESTS
import org.openmined.syft.networking.requests.SocketAPI
import org.openmined.syft.networking.requests.WebRTCMessageTypes
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
                        is NetworkMessage.MessageReceived -> emitMessage(deserializeSocket(it.message))
                    }
                }
                .subscribeOn(schedulers.computeThreadScheduler)
                .observeOn(schedulers.calleeThreadScheduler)
                .subscribe())
        socketClientSubscribed.set(true)
    }

    override fun authenticate(): Single<AuthenticationSuccess> {

        val sendStatus = syftWebSocket.send(appendType(REQUESTS.AUTHENTICATION))
        return messageProcessor.onBackpressureLatest()
                .filter { it is AuthenticationSuccess }
                .map { it as AuthenticationSuccess }
                .firstOrError()
    }

    override fun getCycle(cycleRequest: CycleRequest): Single<CycleResponseData> {
        val sendStatus = syftWebSocket.send(appendType(REQUESTS.CYCLE_REQUEST, cycleRequest))

        return messageProcessor.onBackpressureBuffer()
                .filter { it is CycleResponseData }
                .filter {
                    when (it) {
                        is CycleResponseData -> SyftJob.JobID(
                            cycleRequest.modelName,
                            cycleRequest.version
                        ).matchWithResponse(it.modelName, it.version)
                        else -> false
                    }
                }.debounce(timeout.toLong(), TimeUnit.MILLISECONDS)
                .map { it as CycleResponseData }
                .firstOrError()
    }
    //todo handle backpressure and first or error
    override fun report(reportRequest: ReportRequest): Single<ReportResponse> {
        syftWebSocket.send(appendType(REQUESTS.REPORT, reportRequest))
        return messageProcessor.onBackpressureDrop()
                .filter { it is ReportResponse }
                .map { it as ReportResponse }
                .firstOrError()
    }
    //todo handle backpressure and first or error
    override fun joinRoom(joinRoomRequest: JoinRoomRequest): Single<JoinRoomResponse> {
        syftWebSocket.send(appendType(WebRTCMessageTypes.WEBRTC_JOIN_ROOM, joinRoomRequest))
        return messageProcessor.onBackpressureBuffer()
                .filter { it is JoinRoomResponse }
                .map { it as JoinRoomResponse }
                .firstOrError()
    }

    //todo handle backpressure and first or error
    override fun internalMessage(internalMessageRequest: InternalMessageRequest): Single<InternalMessageResponse> {
        syftWebSocket.send(appendType(REQUESTS.WEBRTC_INTERNAL, internalMessageRequest))
        return messageProcessor.onBackpressureBuffer()
                .filter { it is InternalMessageResponse }
                .map { it as InternalMessageResponse }
                .first(null)
    }

    private fun emitMessage(response: SocketResponse) {
        messageProcessor.offer(response.data)
    }

    private fun deserializeSocket(socketMessage: String): SocketResponse {
        return Json.parse(SocketResponse.serializer(), socketMessage)
    }

    private fun appendType(types: MessageTypes, data: NetworkModels? = null) = json {
        TYPE to types.value
        if (data != null)
            DATA to data
    }
}