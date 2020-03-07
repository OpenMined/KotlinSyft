package org.openmined.syft.networking.clients

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import kotlinx.serialization.json.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.openmined.syft.networking.requests.Protocol
import java.util.concurrent.TimeUnit


const val TYPE = "type"
const val DATA = "data"
private const val SOCKET_CLOSE_CLIENT = 1000

@ExperimentalUnsignedTypes
class SyftWebSocket(
    protocol: Protocol,
    address: String,
    keepAliveTimeout: UInt
) {

    private var request = Request.Builder()
            .url("$protocol://$address")
            .build()
    private var client = OkHttpClient.Builder()
            .pingInterval(keepAliveTimeout.toLong(), TimeUnit.MILLISECONDS)
            .build()
    private val syftSocketListener = SyftSocketListener()
    private val statusPublishProcessor: PublishProcessor<NetworkMessage> =
            PublishProcessor.create<NetworkMessage>()

    private lateinit var webSocket: WebSocket

    fun start(): Flowable<NetworkMessage> {
        connect()
        return statusPublishProcessor.onBackpressureBuffer()
    }

    /**
     * Send the data over the Socket connection to PyGrid
     */
    fun send(message: JsonObject) = webSocket.send(message.toString())

    fun close() = webSocket.close(SOCKET_CLOSE_CLIENT, "Socket closed by client")


    private fun connect() {
        webSocket = client.newWebSocket(request, syftSocketListener)
    }

    inner class SyftSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            this@SyftWebSocket.webSocket = webSocket
            statusPublishProcessor.offer(NetworkMessage.SocketOpen)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            statusPublishProcessor.offer(NetworkMessage.MessageReceived(text))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            statusPublishProcessor.offer(NetworkMessage.SocketError(t))
            // TODO we probably need here some backoff strategy
            connect()
        }
    }
}

