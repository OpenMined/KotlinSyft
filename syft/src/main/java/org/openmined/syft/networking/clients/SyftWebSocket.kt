package org.openmined.syft.networking.clients

import androidx.annotation.VisibleForTesting
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.PublishProcessor
import kotlinx.serialization.json.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.openmined.syft.networking.requests.NetworkingProtocol
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

private const val TAG = "SyftWebSocket"

// Used in serializing data to be passed over the network
internal const val TYPE = "type"
internal const val DATA = "data"

// Code used to close web socket connection
private const val SOCKET_CLOSE_CLIENT = 1000

// Max retry count to reconnect
private const val MAX_RETRY_CONNECTS = 8

// Max timeout after retry
private const val MAX_RETRY_TIMEOUT = 20000L

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
) : Disposable {

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
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var syftSocketListener = SyftSocketListener()

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
     *  Control socket connection and emit new socket
     */
    private val socketStatusProcessor = PublishProcessor.create<WebSocket>()

    /**
     * Check to manage resource usage
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Volatile
    internal var isConnected = AtomicBoolean(false)

    /**
     * Manage and free used resource
     */
    private lateinit var connectionDisposable: Disposable

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
    @VisibleForTesting
    internal fun close() = webSocket?.let {
        isConnected.set(false)
        it.close(SOCKET_CLOSE_CLIENT, "Socket closed by client")
    } ?: false

    /**
     * Create new web socket connection
     * */
    private fun connect() {
        if (isConnected.get()) return

        var retryDelay = 1000L

        connectionDisposable = socketStatusProcessor.retryWhen { errors ->
            errors.zipWith(
                Flowable.range(1, MAX_RETRY_CONNECTS + 1),
                BiFunction<Throwable, Int, Int> { error: Throwable, retryCount: Int ->
                    if (retryCount > MAX_RETRY_CONNECTS) throw error
                    statusPublishProcessor.offer(NetworkMessage.SocketError(error))
                    retryCount
                }
            ).flatMap {
                retryDelay = min(retryDelay * 2, MAX_RETRY_TIMEOUT)
                Flowable.timer(retryDelay, TimeUnit.MILLISECONDS)
            }
        }.subscribe {
            if (isConnected.get()) return@subscribe
            webSocket = it
            isConnected.set(true)
        }
        socketStatusProcessor.offer(client.newWebSocket(request, syftSocketListener))
    }

    override fun dispose() {
        connectionDisposable.dispose()
        if (isDisposed) {
            close()
        }
    }

    override fun isDisposed(): Boolean = isConnected.get()

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
             isConnected.set(false)
             socketStatusProcessor.onError(t)
        }
    }
}

