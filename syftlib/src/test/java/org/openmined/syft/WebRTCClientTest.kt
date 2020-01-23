package org.openmined.syft

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory

private const val TAG = "WebRTC test"

class WebRTCClientTest {

    @Mock
    lateinit var peerConnectionFactory: PeerConnectionFactory
    @Mock
    lateinit var peerConfig: PeerConnection.RTCConfiguration
    @Mock
    lateinit var socket: Socket

    @InjectMocks
    private lateinit var cut: WebRTCClient

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `Given a workerId and a scopeId when the client starts it sends it through the socket`() {
        val workerId = "workerId"
        val scopeId = "scopeId"

        cut.start(workerId, scopeId)

        verify(socket).send(WEBRTC_JOIN_ROOM, "{$workerId,$scopeId}")
    }
}