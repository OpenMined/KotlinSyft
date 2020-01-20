package org.openmined.kotlinsyft

import android.util.Log
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension


private const val TAG = "WebRTC test"

@ExtendWith(MockitoExtension::class)
class WebRTCClientTest {

    @Mock
    lateinit var socket: Socket

    lateinit var rtcClient: WebRTCClient
    @Test
    fun start() {
        Log.v(TAG, "start() sends message to join the scope")
        rtcClient.start("joinTestId", "joinTestScope")

    }

    @Test
    fun stop() {
    }

    @Test
    fun sendMessage() {
    }

    @Test
    fun receiveNewPeer() {

    }

    @Test
    fun receiveInternalMessage() {
    }

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
        Mockito.`when`(socket.send(anyString(), anyString()))
    }

}