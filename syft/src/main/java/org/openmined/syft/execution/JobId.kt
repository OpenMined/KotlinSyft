package org.openmined.syft.execution

import java.security.MessageDigest

/**
 * A uniquer identifier class for the job
 * @property modelName The name of the model used in the job while querying PyGrid
 * @property version The model version in PyGrid
 */
data class JobId(
    val modelName: String,
    val version: String? = null
) {

    /**
     * Unique hash
     */
    val id: String get() = hashId()

    /**
    * Generate a unique hash ID based job name and version
    * */
    private fun hashId(): String {
        val input = "$modelName:$version"
        val bytes = MessageDigest
                .getInstance("MD5")
                .digest(input.toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }
}