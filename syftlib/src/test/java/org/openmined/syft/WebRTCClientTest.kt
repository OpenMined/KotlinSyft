package org.openmined.syft

import kotlinx.serialization.json.json
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
    lateinit var signallingClient: SignallingClient

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
        val expected = json {
            "workerId" to workerId
            "scopeId" to scopeId
        }
        cut.start(workerId, scopeId)
        verify(signallingClient).send(WEBRTC_JOIN_ROOM, expected)
    }
}