package org.openmined.syft.networking.clients

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.openmined.syft.networking.requests.HttpAPI
import org.openmined.syft.networking.requests.NetworkingProtocol
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

/**
 * Creates a retrofit api client for PyGrid endpoints.
 *
 * @property baseUrl url of the server hosting the pyGrid instance.
 * @see org.openmined.syft.networking.requests.HttpAPI for endpoint description.
 */
class HttpClient(baseUrl: String) {
    val apiClient: HttpAPI = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .baseUrl("${NetworkingProtocol.HTTP}://$baseUrl")
            .build().create(HttpAPI::class.java)

}