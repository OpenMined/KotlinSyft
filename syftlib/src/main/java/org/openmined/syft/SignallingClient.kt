package org.openmined.syft

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.json
import okhttp3.*
import org.openmined.syft.interfaces.SignallingInterface
import java.util.concurrent.TimeUnit

private const val TAG = "WebRTC Signalling Client"

internal class SignallingClient(
    private val signallingInterface: SignallingInterface,
    private val workerId: String,
    private val keepAliveTimeout: Int = 20000,
    private val url: String,
    private val port: Int
) {
    private lateinit var request: Request
    private lateinit var client: OkHttpClient
    private var isConnected = false
    private lateinit var webSocket: WebSocket
    private val syftSocketListener = SyftSocketListener()

    private val statusPublishProcessor: PublishProcessor<String> = PublishProcessor.create<String>()

    fun start(): Flowable<String> {
        client = OkHttpClient.Builder()
            .pingInterval(keepAliveTimeout.toLong(), TimeUnit.MILLISECONDS)
            .build()
        request = Request.Builder()
            .url(url)
            .build()
        connect()
        return statusPublishProcessor.onBackpressureBuffer()
    }

    inner class SyftSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            this@SignallingClient.webSocket = webSocket
            isConnected = true
            // TODO Could actually be replaced by just publishing
            signallingInterface.onOpen()
            statusPublishProcessor.offer("We are open for business!")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // TODO Could actually be replaced by just publishing
            signallingInterface.onMessage(text)
            statusPublishProcessor.offer("Received new message $text")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            isConnected = false
            webSocket.close(1001, "Some error")
            // TODO we probably need here some kind of progressive
            connect()
        }
    }

    private fun connect() {
        webSocket = client.newWebSocket(request, syftSocketListener)
    }

    /**
     * Send the data over the Socket connection to PyGrid
     */
    fun send(type: String, data: kotlinx.serialization.json.JsonObject) {
        if (isConnected) {
            val message = json {
                "type" to type
                "data" to data.content.toMutableMap().replace("workerId", JsonPrimitive(workerId))
            }.toString()

            webSocket.send(message)

            statusPublishProcessor.offer("Message sent")
        }
    }

    fun close() {
        if (isConnected) {
            webSocket.close(1000, "Socket close by client")
        }
        statusPublishProcessor.offer("Closing for today")
    }
}
