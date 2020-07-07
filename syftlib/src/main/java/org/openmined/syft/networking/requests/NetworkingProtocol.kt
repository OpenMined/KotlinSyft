package org.openmined.syft.networking.requests

internal sealed class NetworkingProtocol {
    object WSS : NetworkingProtocol() {
        override fun toString(): String {
            return "ws"
        }
    }

    object HTTP:NetworkingProtocol(){
        override fun toString(): String {
            return "http"
        }
    }

    object HTTPS : NetworkingProtocol() {
        override fun toString(): String {
            return "https"
        }
    }
}