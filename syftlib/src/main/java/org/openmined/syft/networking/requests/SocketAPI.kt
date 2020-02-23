package org.openmined.syft.networking.requests

import io.reactivex.Single
import okhttp3.ResponseBody

interface SocketAPI : CommunicationAPI {

    fun joinRoom(workerId: String, scopeId: String): Single<ResponseBody>

    fun internalMessage(
        workerId: String,
        scopeId: String,
        target: String,
        type: WebRTCMessageTypes,
        message: String
    ): Single<ResponseBody>
}