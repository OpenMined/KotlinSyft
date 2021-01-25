package org.openmined.syft.execution

import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

@ExperimentalUnsignedTypes
data class JobModel(
    val modelName: String,
    val version: String? = null,
    val plans: ConcurrentHashMap<String, Plan>,
    val protocols: ConcurrentHashMap<String, Protocol>,
    val requiresSpeedTest: AtomicBoolean,
    val isDisposed: AtomicBoolean
) {

    val id: String get() = hashId()

    private fun hashId(): String {
        val input = "$modelName:$version"
        val bytes = MessageDigest
                .getInstance("MD5")
                .digest(input.toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }
}