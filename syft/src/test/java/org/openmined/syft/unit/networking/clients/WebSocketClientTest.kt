package org.openmined.syft.unit.networking.clients

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.serialization.json.json
import org.junit.Test
import org.openmined.syft.networking.clients.SyftWebSocket
import org.openmined.syft.networking.requests.NetworkingProtocol


@ExperimentalUnsignedTypes
internal class WebSocketClientTest {

    private val webSocket = SyftWebSocket(NetworkingProtocol.HTTP, "pygrid.url", 2000u)

    @Test
    fun `verify calling start function should connect and initialized the web socket`() {
        val processor = webSocket.start()
        processor.test().assertEmpty()
        processor.test().assertNotComplete()
    }

    @Test
    fun `verify calling send function should sending message through the socket connection`() {
        webSocket.start()
        val message = json {
            "data" to "data"
        }
        assert(webSocket.send(message))
    }

    @Test
    fun `verify disposing the socket will close the connection`() {
        webSocket.start()
        assert(webSocket.isConnected.get())

        webSocket.dispose()
        assert(!webSocket.isConnected.get())
    }

    @Test
    fun `socket connection should reconnect after failure`() {
        val listener = mock<SyftWebSocket.SyftSocketListener>()
        whenever(listener.onFailure(mock(), mock(), mock())).thenAnswer{ throw Exception() }

        webSocket.syftSocketListener = listener
        assert(!webSocket.isConnected.get())

        webSocket.start()
        assert(webSocket.isConnected.get())

        listener.onFailure(mock(), mock(), mock())
        assert(webSocket.isConnected.get())
    }

}