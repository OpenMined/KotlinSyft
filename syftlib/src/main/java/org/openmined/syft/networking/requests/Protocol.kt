package org.openmined.syft.networking.requests

sealed class Protocol {
    object WSS : Protocol() {
        override fun toString(): String {
            return "ws"
        }
    }

    object HTTPS : Protocol() {
        override fun toString(): String {
            return "https"
        }
    }
}