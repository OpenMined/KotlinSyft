package org.openmined.syft.domain

sealed class Protocol {
    object WSS : Protocol() {
        override fun toString(): String {
            return "wss"
        }
    }
}