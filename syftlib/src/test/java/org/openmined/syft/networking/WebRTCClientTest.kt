package org.openmined.syft.networking

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.openmined.syft.networking.clients.SyftWebSocket
import org.openmined.syft.networking.clients.WebRTCClient
import org.openmined.syft.networking.clients.SocketClient
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory

private const val TAG = "WebRTC test"

@ExperimentalUnsignedTypes
class WebRTCClientTest {

    @Mock
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    @Mock
    private lateinit var peerConfig: PeerConnection.RTCConfiguration
    @Mock
    private lateinit var syftWebSocket: SyftWebSocket

    @InjectMocks
    private lateinit var cut: WebRTCClient

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    @ExperimentalUnsignedTypes
    fun `Given a workerId and a scopeId when the client starts it sends it through the socket`() {
        val workerId = "workerId"
        val scopeId = "scopeId"
        cut.start(workerId, scopeId)

        verify(syftWebSocket).send(SocketClient.joinRoom(workerId,scopeId))
    }
}
