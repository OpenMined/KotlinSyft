package org.openmined.syft.domain

import android.content.Context
import android.net.NetworkCapabilities
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.networking.requests.CommunicationAPI
import org.openmined.syft.networking.requests.SocketAPI
import org.openmined.syft.threading.ProcessSchedulers
import java.io.File

@ExperimentalUnsignedTypes
class SyftConfiguration internal constructor(
    val context: Context,
    val networkingSchedulers: ProcessSchedulers,
    val computeSchedulers: ProcessSchedulers,
    val filesDir: File,
    val networkConstraints: List<Int>,
    private val socketClient: SocketClient,
    private val httpClient: HttpClient,
    private val maxConcurrentJobs: Int,
    private val messagingClient: NetworkingClients,
    val cacheTimeOut: Long = 100000
) {
    companion object {
        fun builder(context: Context, baseUrl: String) = SyftConfigBuilder(context, baseUrl)
    }

    fun getDownloader() = httpClient.apiClient

    fun getSignallingClient(): CommunicationAPI = when (messagingClient) {
        NetworkingClients.HTTP -> httpClient.apiClient
        NetworkingClients.SOCKET -> socketClient
    }

    fun getWebRTCSignallingClient(): SocketAPI = socketClient

    class SyftConfigBuilder(private val context: Context, private val baseUrl: String) {

        private var networkingSchedulers: ProcessSchedulers = object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.io()
            override val calleeThreadScheduler: Scheduler
                get() = AndroidSchedulers.mainThread()
        }

        private var computeSchedulers: ProcessSchedulers = object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.io()
            override val calleeThreadScheduler: Scheduler
                get() = AndroidSchedulers.mainThread()
        }
        private var socketClient = SocketClient(baseUrl, 20000u, networkingSchedulers)
        private var httpClient = HttpClient(baseUrl)
        private var filesDir = context.filesDir
        private var maxConcurrentJobs: Int = 1
        private var messagingClient: NetworkingClients = NetworkingClients.SOCKET
        private val networkConstraints = mutableMapOf(
            NetworkCapabilities.NET_CAPABILITY_INTERNET to true,
            NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED to true,
            NetworkCapabilities.NET_CAPABILITY_NOT_METERED to true
        )

        fun build(): SyftConfiguration {
            val constraintList = networkConstraints.filterValues { it }.keys.toList()
            return SyftConfiguration(
                context,
                networkingSchedulers,
                computeSchedulers,
                filesDir,
                constraintList,
                socketClient,
                httpClient,
                maxConcurrentJobs,
                messagingClient
            )
        }

        fun enableMeteredData(): SyftConfigBuilder {
            if (networkConstraints.containsKey(NetworkCapabilities.NET_CAPABILITY_NOT_METERED))
                networkConstraints.remove(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            return this
        }

        fun setNetworkingScheduler(scheduler: ProcessSchedulers): SyftConfigBuilder {
            this.networkingSchedulers = scheduler
            return this
        }

        fun setComputeScheduler(computeSchedulers: ProcessSchedulers): SyftConfigBuilder {
            this.computeSchedulers = computeSchedulers
            return this
        }

        fun setMaxConcurrentJobs(numJobs: Int): SyftConfigBuilder {
            this.maxConcurrentJobs = numJobs
            return this
        }

        fun setFilesDir(filesDir: File): SyftConfigBuilder {
            this.filesDir = filesDir
            return this
        }
    }

    enum class NetworkingClients {
        HTTP, SOCKET
    }
}