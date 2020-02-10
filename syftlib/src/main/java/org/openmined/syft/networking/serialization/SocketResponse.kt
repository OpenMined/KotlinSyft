package org.openmined.syft.networking.serialization

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializable
data class SocketResponse(
    val type: String,
    val data: RequestResponseBody
) {
    @Serializer(forClass = SocketResponse::class)
    companion object : KSerializer<SocketResponse> {
    override val descriptor: SerialDescriptor
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun deserialize(decoder: Decoder): SocketResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun serialize(encoder: Encoder, obj: SocketResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
}