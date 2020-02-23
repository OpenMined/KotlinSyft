package org.openmined.syft.networking.clients

import android.util.Log
import io.reactivex.Single
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import okhttp3.ResponseBody
import org.openmined.syft.networking.datamodels.AuthenticationSuccess
import org.openmined.syft.networking.datamodels.CycleRequest
import org.openmined.syft.networking.datamodels.CycleResponseData
import org.openmined.syft.networking.datamodels.ReportRequest
import org.openmined.syft.networking.datamodels.ReportResponse
import org.openmined.syft.networking.datamodels.SocketResponse
import org.openmined.syft.networking.requests.MessageTypes
import org.openmined.syft.networking.requests.Protocol
import org.openmined.syft.networking.requests.REQUESTS
import org.openmined.syft.networking.requests.SocketAPI
import org.openmined.syft.networking.requests.WebRTCMessageTypes
import org.openmined.syft.threading.ProcessSchedulers
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "SocketClient"

@ExperimentalUnsignedTypes
class SocketClient(baseUrl: String, private val schedulers: ProcessSchedulers) : SocketAPI {

    //Choosing stable kotlin serialization over default
    private val Json = Json(JsonConfiguration.Stable)

    private val syftWebSocket = SyftWebSocket(Protocol.WSS, baseUrl)
    @Volatile
    private var socketClientSubscribed = AtomicBoolean(false)

    val disposable = syftWebSocket.start()
            .map {
                when (it) {
                    is NetworkMessage.SocketOpen -> {
                        authenticate()
                    }
                    is NetworkMessage.SocketClosed -> Log.d(TAG, "Socket was closed successfully")
                    is NetworkMessage.SocketError -> Log.e(TAG, "socket error", it.throwable)
                    is NetworkMessage.MessageReceived -> handleResponse(deserializeSocket(it.message))
                    is NetworkMessage.MessageSent -> println("Message sent successfully")
                }
            }
            .subscribeOn(schedulers.computeThreadScheduler)
            .observeOn(schedulers.calleeThreadScheduler)
            .subscribe()

    private fun initiateSocketIfEmpty() {
        if (socketClientSubscribed.get())
            return

        compositeDisposable.add(syftWebSocket.start()
                .map {
                    when (it) {
                        is NetworkMessage.SocketOpen -> {
                            syftWebSocket.send(SocketClient.authenticate())
                        }
                        is NetworkMessage.SocketClosed -> Log.d(
                            org.openmined.syft.TAG,
                            "Socket was closed successfully"
                        )
                        is NetworkMessage.SocketError -> Log.e(
                            org.openmined.syft.TAG,
                            "socket error",
                            it.throwable
                        )
                        is NetworkMessage.MessageReceived -> handleResponse(
                            SocketClient.deserializeSocket(
                                it.message
                            )
                        )
                        is NetworkMessage.MessageSent -> println("Message sent successfully")
                    }
                }
                .subscribeOn(schedulers.computeThreadScheduler)
                .observeOn(schedulers.calleeThreadScheduler)
                .subscribe())
        socketClientSubscribed.set(true)
    }

    override fun authenticate(): Single<AuthenticationSuccess> {
        return Single.create<AuthenticationSuccess> { emitter ->
            syftWebSocket.send(appendType(REQUESTS.AUTHENTICATION))
            emitter.onSuccess()
        }
    }

    override fun getCycle(cycleRequest: CycleRequest): Single<CycleResponseData> {
        val data = json {
            "worker_id" to workerId
            "model" to syftJob.modelName
            "ping" to ping
            "download" to download
            "upload" to upload
            if (syftJob.version != null)
                "version" to syftJob.version
        }
        return appendType(REQUESTS.CYCLE_REQUEST, data)
    }

    override fun report(reportRequest: ReportRequest): Single<ReportResponse> {
        val data = json {
            "worker_id" to workerId
            "request_key" to requestKey
            "diff" to diff
        }
        return appendType(REQUESTS.REPORT, data)
    }

    override fun joinRoom(workerId: String, scopeId: String): Single<ResponseBody> {
        val data = json {
            "worker_id" to workerId
            "scope_id" to scopeId
        }
        return appendType(
            WebRTCMessageTypes.WEBRTC_JOIN_ROOM, data
        )
    }

    override fun internalMessage(
        workerId: String,
        scopeId: String,
        target: String,
        type: WebRTCMessageTypes,
        message: String
    ): Single<ResponseBody> {
        val data = json {
            "worker_id" to workerId
            "scope_id" to scopeId
            "to" to target
            "type" to type.value
            "data" to message
        }
        return appendType(REQUESTS.WEBRTC_INTERNAL, data)
    }

    fun deserializeSocket(socketMessage: String): SocketResponse {
        return Json.parse(SocketResponse.serializer(), socketMessage)
    }

    private fun appendType(types: MessageTypes, data: JsonObject? = null) = json {
        TYPE to types.value
        if (data != null)
            DATA to data
    }
}