package org.openmined.syft.datamodels

import java.security.MessageDigest

/**
 * A uniquer identifier class for the job
 * @property modelName The name of the model used in the job while querying PyGrid
 * @property version The model version in PyGrid
 */
class JobDataModel(
    val modelName: String,
    val version: String? = null
) {

    /**
     * Unique hash
     */
    val id: String get() = hashId()

    /**
     * Check if two [JobDataModel] are same. Matches both model names and version if [version] is not null for param and current jobId.
     * @param modelName the modelName of the jobId which has to be compared with the current object
     * @param version the version of the jobID which ahs to be compared with the current jobId
     * @return true if JobId match
     * @return false otherwise
     */
    fun matchWithResponse(modelName: String, version: String? = null) =
            if (version.isNullOrEmpty() || this.version.isNullOrEmpty())
                this.modelName == modelName
            else
                (this.modelName == modelName) && (this.version == version)


    private fun hashId(): String {
        val input = "$modelName:$version"
        val bytes = MessageDigest
                .getInstance("MD5")
                .digest(input.toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }
}