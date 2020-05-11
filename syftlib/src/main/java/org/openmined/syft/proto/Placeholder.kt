package org.openmined.syft.proto

import org.openmined.syftproto.execution.v1.PlaceholderOuterClass
import org.openmined.syftproto.types.syft.v1.IdOuterClass


// Placeholder class is used to reference the order of the tensors in the plan.
data class Placeholder(
    val id: IdOuterClass.Id,
    val tags: List<String>,
    val description: String
) {
    companion object {
        // Generate Placeholder object by parsing PlaceholderOuterClass.Placeholder object
        fun deserialize(protobufPlaceholder: PlaceholderOuterClass.Placeholder) = Placeholder(
            protobufPlaceholder.id,
            protobufPlaceholder.tagsList,
            protobufPlaceholder.description
        )
    }

    //Generate PlaceholderOuterClass.Placeholder object
    fun serialize(): PlaceholderOuterClass.Placeholder = PlaceholderOuterClass.Placeholder
            .newBuilder()
            .setId(id)
            .setDescription(description)
            .addAllTags(tags).build()
}