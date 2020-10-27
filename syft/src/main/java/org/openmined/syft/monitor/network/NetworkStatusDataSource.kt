package org.openmined.syft.monitor.network

import android.accounts.NetworkErrorException
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.BroadCastListener
import org.openmined.syft.monitor.StateChangeMessage
import org.openmined.syft.networking.requests.HttpAPI
import java.io.File
import java.io.InputStream
import java.util.Random

private const val MB = 1024 * 1024
private const val SPEED_BUFFER_WINDOW = 20
private const val SPEED_MULTIPLICATION_FACTOR = 10
private const val MAX_SPEED_TESTING_BYTES = MB * 8
private const val TAG = "NetworkStateEvaluator"

@ExperimentalUnsignedTypes
internal class NetworkStatusRealTimeDataSource internal constructor(
    private val downloader: HttpAPI,
    private val filesDir: File,
    private val networkRequest: NetworkRequest,
    private val networkManager: ConnectivityManager,
    private val statusProcessor: PublishProcessor<StateChangeMessage> = PublishProcessor.create()
) : BroadCastListener {

    companion object {
        fun initialize(configuration: SyftConfiguration): NetworkStatusRealTimeDataSource {
            val networkManager = configuration.context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkRequestBuilder = NetworkRequest.Builder()
                    .addTransportType(configuration.transportMedium)
            configuration.networkConstraints.forEach { networkRequestBuilder.addCapability(it) }
            val networkRequest = networkRequestBuilder.build()

            return NetworkStatusRealTimeDataSource(
                configuration.getDownloader(),
                configuration.filesDir,
                networkRequest,
                networkManager
            )
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            statusProcessor.offer(StateChangeMessage.NetworkStatus(false))
        }

        override fun onAvailable(network: Network?) {
            statusProcessor.offer(StateChangeMessage.NetworkStatus(true))
        }
    }

    override fun subscribeStateChange(): Flowable<StateChangeMessage> {
        networkManager.registerNetworkCallback(networkRequest, networkCallback)
        return statusProcessor.onBackpressureLatest()
    }

    override fun unsubscribeStateChange() {
        networkManager.unregisterNetworkCallback(networkCallback)
        statusProcessor.onComplete()
    }

    fun getNetworkValidity(constraints: List<Int>): Boolean {
        networkManager.getNetworkCapabilities(networkManager.activeNetwork)?.let { capabilities ->
            return constraints.all { capabilities.hasCapability(it) }
        } ?: throw Exception("Unknown network. Cannot detect network properties")
    }

    suspend fun updatePing(workerId: String): Int {
        val start = System.currentTimeMillis()
        val pingResponse = downloader.checkPing(
            workerId = workerId,
            random = Random().ints(128).toString()
        )
        return if (pingResponse.code() == 200 && pingResponse.body()?.error == null) {
            val ping = (System.currentTimeMillis() - start).toInt()
            Log.d(TAG, "Ping is $ping ms")
            ping
        }
        // TODO Error Management Else error
        else throw NetworkErrorException("unable to get ping")
    }

    suspend fun updateUploadSpeed(workerId: String): Float {
        val fileSize = 64
        val file = File(filesDir, "uploadFile").apply {
            bufferedWriter().use { output ->
                repeat(fileSize) {
                    output.write("x".repeat(MB))
                }
            }
        }
        val requestFile = file.asRequestBody("text/plain".toMediaType())
        val body = MultipartBody.Part.createFormData("sample", file.name, requestFile)
        val description = "uploadFile".toRequestBody()
        val start = System.currentTimeMillis() / 1000.0f

        val uploadSpeedResponse = downloader.uploadSpeedTest(
            workerId, Random().ints(128).toString(),
            description,
            body
        )

        return if (uploadSpeedResponse.body()?.error == null && uploadSpeedResponse.code() == 200) {
            var speed = fileSize * 1024 / (System.currentTimeMillis() / 1000.0f - start)
            if (speed > 100000)
            //capping infinity
                speed = 100000.0f
            Log.d(TAG, "Upload Speed is $speed KBps")
            file.delete()
            speed
        } else {
            file.delete()
            // TODO Error Management
            throw NetworkErrorException("unable to verify upload speed")
        }
    }

    suspend fun updateDownloadSpeed(workerId: String): Float {
        val downloadSpeedResponse = downloader
                .downloadSpeedTest(workerId, Random().ints(128).toString())
        val evaluateDownloadSpeed = evaluateDownloadSpeed(downloadSpeedResponse.body())
        Log.d(TAG, "Download Speed is $evaluateDownloadSpeed KBps")
        return evaluateDownloadSpeed
    }

    private suspend fun evaluateDownloadSpeed(input: ResponseBody?): Float {
        if (input == null) {
            throw UninitializedPropertyAccessException()
        }

        var begin = 0
        var bufferSize = DEFAULT_BUFFER_SIZE
        val avgSpeedWindow = Array(SPEED_BUFFER_WINDOW) { 0.0f }
        var start = System.currentTimeMillis()
        input.byteStream().use { inputStream ->
            while (true) {
                val count = inputStream.readNBuffers(bufferSize)
                if (count == 0)
                    break
                val timeTaken = (System.currentTimeMillis() - start) / 1000.0f
                if (timeTaken < 0.5) {
                    bufferSize = Integer.min(
                        bufferSize * SPEED_MULTIPLICATION_FACTOR,
                        MAX_SPEED_TESTING_BYTES
                    )
                    continue
                }
                val newSpeed = bufferSize / (timeTaken * 1024)
                if (begin % SPEED_BUFFER_WINDOW == 0) {
                    val avg = avgSpeedWindow.sum() / SPEED_BUFFER_WINDOW
                    val deviation = avg - avgSpeedWindow.min()!!
                    if (deviation < 20 && avg > 0) {
                        break
                    }
                }
                avgSpeedWindow[begin % SPEED_BUFFER_WINDOW] = newSpeed
                begin += 1
                start = System.currentTimeMillis()
            }

            return avgSpeedWindow.sum() / Integer.min(
                begin,
                SPEED_BUFFER_WINDOW
            )
        }
    }

    private fun InputStream.readNBuffers(
        n: Int,
        bufferArray: ByteArray? = null
    ): Int {
        val smallBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var count = 0
        for (i in 0..(n / DEFAULT_BUFFER_SIZE)) {
            val line = this.read(smallBuffer)
            if (line == -1)
                break
            if (bufferArray != null) {
                val offset = count % bufferArray.size
                if (offset != count)
                    Log.w(TAG, "Overriding buffer values due to overflow")
                val endIdx = Integer.min(line, bufferArray.size - offset)
                if (endIdx != DEFAULT_BUFFER_SIZE)
                    smallBuffer.sliceArray(0..endIdx).copyInto(bufferArray, offset)
                else
                    smallBuffer.copyInto(bufferArray, offset)
            }
            count += line
        }
        return count
    }
}