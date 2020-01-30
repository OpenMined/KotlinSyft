package org.openmined.syft.network

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.json
import okhttp3.*
import java.util.concurrent.TimeUnit

private const val SOCKET_CLOSE_CLIENT = 1000
private const val SOCKET_CLOSE_ERROR = 1001

internal class SignallingClient(
    private val workerId: String,
    private val url: String,
    private val keepAliveTimeout: Int = 20000
) {
    private lateinit var request: Request
    private lateinit var client: OkHttpClient
    private lateinit var webSocket: WebSocket
    private val syftSocketListener = SyftSocketListener()

    private val statusPublishProcessor: PublishProcessor<NetworkMessage> =
        PublishProcessor.create<NetworkMessage>()

    fun start(): Flowable<NetworkMessage> {
        client = OkHttpClient.Builder()
            .pingInterval(keepAliveTimeout.toLong(), TimeUnit.MILLISECONDS)
            .build()
        request = Request.Builder()
            .url(url)
            .build()
        connect()
        return statusPublishProcessor.onBackpressureBuffer()
    }

    /**
     * Send the data over the Socket connection to PyGrid
     */
    fun send(type: String, data: JsonObject) {
        val message = json {
            "type" to type
            "data" to data.content.toMutableMap().replace("workerId", JsonPrimitive(workerId))
        }.toString()

        if (webSocket.send(message)) {
            statusPublishProcessor.offer(NetworkMessage.MessageSent)
        }
    }

    fun close() {
        if (webSocket.close(SOCKET_CLOSE_CLIENT, "Socket closed by client")) {
            statusPublishProcessor.offer(NetworkMessage.SocketClosed)
        }
    }

    private fun connect() {
        webSocket = client.newWebSocket(request, syftSocketListener)
    }

    inner class SyftSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            this@SignallingClient.webSocket = webSocket
            statusPublishProcessor.offer(NetworkMessage.SocketOpen)
        }

        override fun onMessage(webSocket: WebSocket, message: String) {
            statusPublishProcessor.offer(NetworkMessage.MessageReceived(message))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            statusPublishProcessor.offer(NetworkMessage.SocketError(t))
            // TODO we probably need here some backoff strategy
            connect()
        }
    }
}

