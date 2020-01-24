package org.openmined.syft

import android.util.Log
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.json
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.Charset
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


const val SOCKET_PING = "socket-ping"
private const val TAG = "WebRTC Signalling Client"

class SignallingClient(
    private val signallingInterface: SignallingInterface,
    private val workerId: String,
    private val keepAliveTimeout: Int = 20000,
    private val url: String,
    private val port: Int
) {
    //creating socket holder. This is connected later in connectSocket()
    private var socket = Socket()
    private lateinit var writer: BufferedWriter
    private lateinit var reader: BufferedReader

    private val disposable: Disposable
    private val responseObservable: Flowable<String>
    private val keepSendingScheduler = Executors.newScheduledThreadPool(1)
    private var sendingThread: Future<*>

    init {
        responseObservable = Flowable.create<String>(
            { emitter ->
                emitter.setCancellable { this.stop() }
                try {
                    connectSocket()
                    while (socket.isConnected) {
                        emitter.onNext(reader.readLine())
                    }
                    emitter.onComplete()
                } catch (e: Exception) {
                    Log.e(TAG, "Socket TimeOut", e)
                    stop()
                    emitter.onError(e)
                }
            }, BackpressureStrategy.BUFFER
        )

        disposable = invokeSocketObserver()
        sendingThread = runKeepAliveSchedule()
    }

    /**
     * Send the data over the Socket connection to PyGrid
     */
    fun send(type: String, data: JsonObject) {
        if (socket.isConnected) {
            sendingThread.cancel(false)

            val message = json {
                "type" to type
                "data" to data.content.toMutableMap().
                    replace("workerId", JsonPrimitive(workerId))
            }
            writer.write(message.toString())
            writer.flush()
            Log.d(TAG, "message sent :$message")
            sendingThread = runKeepAliveSchedule()
        } else {
            disposable.dispose()
            invokeSocketObserver()
        }

    }

    private fun runKeepAliveSchedule(): ScheduledFuture<*> {
        return keepSendingScheduler.scheduleAtFixedRate(
            {
                if (socket.isConnected)
                    send(SOCKET_PING, json { })
            },
            keepAliveTimeout.toLong(),
            keepAliveTimeout.toLong(),
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * @return void but sets up SignallingClient class properties
     */
    private fun connectSocket() {
        Log.d(TAG, "Opening Socket Connection")
        socket.connect(InetSocketAddress(url, port))
        Log.d(TAG, "Socket Connected")
        socket.keepAlive = true
        socket.soTimeout = keepAliveTimeout
        writer = socket.getOutputStream().bufferedWriter(Charset.defaultCharset())
        reader = socket.getInputStream().bufferedReader(Charset.defaultCharset())
        signallingInterface.onOpen()
    }

    /**
     * Whenever a subscriber is subscribed to Observable, a new invocation happens
     * initiating socket connection and setting up socket reader and writer
     *
     * This function is also called when connection is no more `Connected`
     * thereby re-establishing connection to PyGrid
     */
    private fun invokeSocketObserver(): Disposable {
        return responseObservable
            .observeOn(signallingInterface.calleeThreadScheduler)
            .subscribeOn(signallingInterface.computeThreadScheduler)
            .subscribe(
                { signallingInterface.onMessage(it) },
                { Log.e(TAG, "we have a socket error!", it) },
                {
                    Log.d(TAG, "Socket disconnected")
                    invokeSocketObserver()
                }
            )
    }

    /**
     * Close the socket
     * Unsubscribe the Observer
     */
    private fun stop() {
        socket.close()
        disposable.dispose()
        Log.d(TAG, "socket connection closed")
        signallingInterface.onClose()
    }

}
