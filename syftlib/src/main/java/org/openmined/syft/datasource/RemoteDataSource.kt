package org.openmined.syft.datasource

import io.reactivex.Single
import org.openmined.syft.networking.clients.HttpClient
import java.io.InputStream

internal class RemoteDataSource(private val httpClient: HttpClient) {

    /**
     * Download the model modelId and offers it as an input stream for further processing.
     * @param workerId
     * @param requestKey
     * @param modelId
     */
    fun downloadModel(workerId: String, requestKey: String, modelId: String): Single<InputStream> {
        return httpClient.apiClient.downloadModel(workerId, requestKey, modelId)
                .map {
                    it.body()?.byteStream()
                }
    }

    fun downloadProtocol(
        workerId: String,
        requestKey: String,
        protocolId: String
    ): Single<InputStream> {
        return httpClient.apiClient.downloadProtocol(workerId, requestKey, protocolId)
                .map {
                    it.body()?.byteStream()
                }
    }

    fun downloadPlan(
        workerId: String,
        requestKey: String,
        planId: String,
        opType: String
    ): Single<InputStream> {
        return httpClient.apiClient.downloadPlan(workerId, requestKey, planId, opType)
                .map {
                    it.body()?.byteStream()
                }
    }
}