package org.openmined.kotlinsyft

import java.net.Socket


class Socket(private val url: String, private val workerId: String, keepAliveTimeout: Int = 20000) {

    val socket = Socket(url, 888)
    val timerId: String? = null

    fun send(type: String, data: String) {
//TODO decide okhttp vs scarlet for websocket connections
    }

    fun stop() {
        socket.close()
    }
}