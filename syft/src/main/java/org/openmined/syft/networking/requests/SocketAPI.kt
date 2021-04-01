package org.openmined.syft.networking.requests

import org.openmined.syft.networking.datamodels.webRTC.InternalMessageRequest
import org.openmined.syft.networking.datamodels.webRTC.InternalMessageResponse
import org.openmined.syft.networking.datamodels.webRTC.JoinRoomRequest
import org.openmined.syft.networking.datamodels.webRTC.JoinRoomResponse

/**
 * Represent WebRTC connection API
 * */
internal interface SocketAPI : CommunicationAPI {

    /**
     * Request joining a federated learning cycle
     * */
    suspend fun joinRoom(joinRoomRequest: JoinRoomRequest): JoinRoomResponse

    /**
     * Send message via PyGrid
     * */
    suspend fun sendInternalMessage(internalMessageRequest: InternalMessageRequest): InternalMessageResponse
}