package org.openmined.syft.datasource

import org.openmined.syft.networking.requests.HttpAPI
import java.io.InputStream

internal class JobRemoteDataSource(private val httpApi: HttpAPI) {

    /**
     * Downloads the model modelId and offers it as an input stream for further processing.
     * @param workerId
     * @param requestKey
     * @param modelId
     */
    suspend fun downloadModel(workerId: String, requestKey: String, modelId: String): InputStream? {
        return httpApi.downloadModel(workerId, requestKey, modelId)
                .body()?.byteStream()
    }

    suspend fun downloadProtocol(
        workerId: String,
        requestKey: String,
        protocolId: String
    ): InputStream? {
        return httpApi.downloadProtocol(workerId, requestKey, protocolId)
                .body()?.byteStream()
    }

    suspend fun downloadPlan(
        workerId: String,
        requestKey: String,
        planId: String,
        opType: String
    ): InputStream? {
        return httpApi.downloadPlan(workerId, requestKey, planId, opType)
                .body()?.byteStream()
    }
}