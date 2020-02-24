package org.openmined.syft.networking.requests

import io.reactivex.Single
import org.openmined.syft.networking.datamodels.webRTC.InternalMessageRequest
import org.openmined.syft.networking.datamodels.webRTC.InternalMessageResponse
import org.openmined.syft.networking.datamodels.webRTC.JoinRoomRequest
import org.openmined.syft.networking.datamodels.webRTC.JoinRoomResponse

interface SocketAPI : CommunicationAPI {

    fun joinRoom(joinRoomRequest: JoinRoomRequest): Single<JoinRoomResponse>

    fun internalMessage(internalMessageRequest: InternalMessageRequest): Single<InternalMessageResponse>
}