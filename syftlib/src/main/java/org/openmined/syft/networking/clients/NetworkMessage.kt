package org.openmined.syft.networking.clients

sealed class NetworkMessage() {
    object SocketClosed : NetworkMessage()
    object SocketOpen : NetworkMessage()
    data class SocketError(val throwable: Throwable) : NetworkMessage()
    object MessageSent : NetworkMessage()
    data class MessageReceived(val message: String) : NetworkMessage()
}