package org.openmined.syft.networking.requests

import io.reactivex.Single
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
    fun joinRoom(joinRoomRequest: JoinRoomRequest): Single<JoinRoomResponse>

    /**
     * Send message via PyGrid
     * */
    fun sendInternalMessage(internalMessageRequest: InternalMessageRequest): Single<InternalMessageResponse>
}