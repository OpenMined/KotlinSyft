package org.openmined.syft.network

sealed class NetworkMessage() {
    object SocketClosed : NetworkMessage()
    object SocketOpen : NetworkMessage()
    data class SocketError(val throwable: Throwable) : NetworkMessage()
    object MessageSent : NetworkMessage()
    data class MessageReceived(val message: String) : NetworkMessage()
}