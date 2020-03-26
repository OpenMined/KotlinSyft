package org.openmined.syft.proto

interface ProtobuffObject {
    fun serialise()
    fun deserialise()
}