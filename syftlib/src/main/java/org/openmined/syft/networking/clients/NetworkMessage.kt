package org.openmined.syft.networking.clients

sealed class NetworkMessage {
    object SocketOpen : NetworkMessage()
    data class SocketError(val throwable: Throwable) : NetworkMessage()
    data class MessageReceived(val message: String) : NetworkMessage()
}