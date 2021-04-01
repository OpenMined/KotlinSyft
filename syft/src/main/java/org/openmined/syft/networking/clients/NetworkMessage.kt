package org.openmined.syft.networking.clients

internal sealed class NetworkMessage {
    object Started : NetworkMessage()
    object SocketOpen : NetworkMessage()
    data class SocketError(val throwable: Throwable) : NetworkMessage()
    data class MessageReceived(val message: String) : NetworkMessage()
}