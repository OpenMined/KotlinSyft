package org.openmined.syft.domain

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
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
class SyftConfiguration private constructor(
    val context: Context,
    baseUrl: String,
    val networkingSchedulers: ProcessSchedulers,
    val computeSchedulers: ProcessSchedulers,
    val filesDir: File,
//todo add network state here
    private val maxConcurrentJobs: Int,
    private val messagingClient: NetworkingClients
) {
    companion object {
        fun builder(context: Context, baseUrl: String) = SyftConfigBuilder(context, baseUrl)
    }

    private val socketClient = SocketClient(baseUrl, 10000u, networkingSchedulers)
    private val httpClient = HttpClient(baseUrl)
    private val batteryStatus: Intent? = context.registerReceiver(
        null,
        IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
    )
    private val networkManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val activityStatus =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun getDownloader() = httpClient.apiClient

    fun getSignallingClient(): CommunicationAPI = when (messagingClient) {
        NetworkingClients.HTTP -> httpClient.apiClient
        NetworkingClients.SOCKET -> socketClient
    }

    fun getWebRTCSignallingClient(): SocketAPI = socketClient


    class SyftConfigBuilder(val context: Context, val baseUrl: String) {

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
        private var filesDir = context.filesDir
        private var maxConcurrentJobs: Int = 1
        private var messagingClient: NetworkingClients = NetworkingClients.SOCKET

        fun build() = SyftConfiguration(
            context,
            baseUrl,
            networkingSchedulers,
            computeSchedulers,
            filesDir,
            maxConcurrentJobs,
            messagingClient
        )

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