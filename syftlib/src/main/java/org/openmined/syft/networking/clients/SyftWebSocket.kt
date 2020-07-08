package org.openmined.syft.networking.clients

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import kotlinx.serialization.json.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.openmined.syft.networking.requests.NetworkingProtocol
import java.util.concurrent.TimeUnit

// Used in serializing data to be passed over the network
internal const val TYPE = "type"
internal const val DATA = "data"
// Code used to close web socket connection
private const val SOCKET_CLOSE_CLIENT = 1000

/**
 * SyftWebSocket initialize and configure Web Socket connection
 * @param protocol {@link org.openmined.syft.networking.requests.NetworkingProtocol NetworkingProtocol} to be used
 * @param address Address to connect
 * @param keepAliveTimeout Timeout period
 * */
@ExperimentalUnsignedTypes
internal class SyftWebSocket(
    protocol: NetworkingProtocol,
    address: String,
    keepAliveTimeout: UInt
) {

    /**
     * Required to create web socket connection
     */
    private var request = Request.Builder()
            .url("$protocol://$address")
            .build()

    /**
     * Initialize the WebSocket instance
     */
    private var client = OkHttpClient.Builder()
            .pingInterval(keepAliveTimeout.toLong(), TimeUnit.MILLISECONDS)
            .build()

    /**
     * Respond to WebSocket life cycle event
     */
    private val syftSocketListener = SyftSocketListener()

    /**
     * Emit messages to subscribers
     */
    private val statusPublishProcessor: PublishProcessor<NetworkMessage> =
            PublishProcessor.create<NetworkMessage>()

    /**
     *  store the web socket connection
     */
    private var webSocket: WebSocket? = null

    /**
     * connect socket to PyGrid, manage back-pressure with emitting messages
     * */
    fun start(): Flowable<NetworkMessage> {
        connect()
        return statusPublishProcessor.onBackpressureBuffer()
    }

    /**
     * Send the data over the Socket connection to PyGrid
     */
    fun send(message: JsonObject) = webSocket?.send(message.toString()) ?: false

    /**
     * Close web socket connection
     * */
    fun close() = webSocket?.close(SOCKET_CLOSE_CLIENT, "Socket closed by client") ?: false

    /**
     * Create new web socket connection
     * */
    private fun connect() {
        webSocket = client.newWebSocket(request, syftSocketListener)
    }


     /**
      * Override WebSocketListener life cycle methods
      */
    inner class SyftSocketListener : WebSocketListener() {

         /**
          * Connection accepted by PyGrid and notify subscribers
          */
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            this@SyftWebSocket.webSocket = webSocket
            statusPublishProcessor.offer(NetworkMessage.SocketOpen)
        }

         /**
          * Message received and emit the message to the subscribers
          */
        override fun onMessage(webSocket: WebSocket, text: String) {
            statusPublishProcessor.offer(NetworkMessage.MessageReceived(text))
        }

         /**
          *  Handle socket failure and notify subscribers. And try to reconnect
          */
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            statusPublishProcessor.offer(NetworkMessage.SocketError(t))
            // TODO we probably need here some backoff strategy
            connect()
        }
    }
}

