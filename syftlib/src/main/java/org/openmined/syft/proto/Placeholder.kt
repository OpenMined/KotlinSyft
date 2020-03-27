package org.openmined.syft.proto

import com.google.protobuf.ProtocolStringList
import org.openmined.syftproto.frameworks.torch.tensors.interpreters.v1.PlaceholderOuterClass
import org.openmined.syftproto.types.syft.v1.IdOuterClass

data class Placeholder(
    val id: IdOuterClass.Id,
    val tags: ProtocolStringList,
    val description: String
) {
    companion object {
        fun deserialize(protobufPlaceholder: PlaceholderOuterClass.Placeholder) = Placeholder(
            protobufPlaceholder.id,
            protobufPlaceholder.tagsList,
            protobufPlaceholder.description
        )
    }

    fun serialize(): PlaceholderOuterClass.Placeholder = PlaceholderOuterClass.Placeholder
            .newBuilder()
            .setId(id)
            .setDescription(description)
            .addAllTags(tags).build()

}