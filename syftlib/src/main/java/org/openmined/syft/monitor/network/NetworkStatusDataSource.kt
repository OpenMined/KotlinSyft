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
import kotlin.Float.Companion.POSITIVE_INFINITY

private const val KB = 1024
private const val MB = KB * KB
private const val MAX_SPEED_TESTS = 3
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

    fun updatePing(workerId: String, networkStatusModel: NetworkStatusModel): Completable {
        val start = System.currentTimeMillis()
        return downloader.checkPing(
            workerId = workerId,
            random = Random().ints(128).toString()
        )
                .flatMapCompletable { response ->
                    if (response.code() == 200 && response.body()?.error == null) {
                        networkStatusModel.ping = (System.currentTimeMillis() - start).toString()
                        Log.d(TAG, "Ping is ${networkStatusModel.ping} ms")
                        Completable.complete()
                    } else
                        Completable.error(NetworkErrorException("unable to get ping"))

                }
    }

    fun updateUploadSpeed(
        workerId: String,
        networkStatusModel: NetworkStatusModel
    ): Completable {
        return getUploadSpeedTester(64 * MB, workerId).flatMapCompletable { speed ->
            val speedCapped = if (speed > 10e10f)
                10e10f
            else
                speed
            networkStatusModel.uploadSpeed = speedCapped.toString()
            Log.d(TAG, "Upload Speed is ${networkStatusModel.uploadSpeed} KBps")
            Completable.complete()
        }
    }

    fun updateDownloadSpeed(workerId: String, networkStatusModel: NetworkStatusModel) =
            downloader
                    .downloadSpeedTest(workerId, Random().ints(128).toString())
                    .flatMap { response ->
                        evaluateDownloadSpeed(response.body())
                    }.flatMapCompletable { speed ->
                        networkStatusModel.downloadSpeed = speed.toString()
                        Log.d(TAG, "Download Speed is ${networkStatusModel.downloadSpeed} KBps")
                        Completable.complete()
                    }

    private fun getUploadSpeedTester(sizeInBytes: Int, workerId: String): Single<Float> {
        val file = File(filesDir, "uploadFile").apply {
            bufferedWriter().use { output ->
                output.write("x".repeat(sizeInBytes))
            }
        }
        val start = System.currentTimeMillis() / 1000.0f
        val description = "uploadFile".toRequestBody()
        val requestFile = file.asRequestBody("text/plain".toMediaType())
        val body = MultipartBody.Part.createFormData("sample", file.name, requestFile)
        return downloader.uploadSpeedTest(
            workerId, Random().ints(128).toString(),
            description,
            body
        )
                .flatMap { response ->
                    if (response.body()?.error == null && response.code() == 200) {
                        Single.just(sizeInBytes / (System.currentTimeMillis() / 1000.0f - start))
                    } else {
                        file.delete()
                        Single.error(NetworkErrorException("unable to verify upload speed"))
                    }
                }
    }

    private fun evaluateDownloadSpeed(input: ResponseBody?): Single<Float> {
        if (input == null)
            return Single.error(UninitializedPropertyAccessException())

        return Single.create { emitter ->
            var bufferSize = DEFAULT_BUFFER_SIZE
            val avgSpeedWindow = mutableListOf<Float>()
            var start = System.currentTimeMillis()
            input.byteStream().use { inputStream ->
                for (idx in (0..MAX_SPEED_TESTS)) {
                    val count = inputStream.readNBuffers(bufferSize)
                    if (count == 0)
                        break
                    val timeTaken = (System.currentTimeMillis() - start) / 1000.0f
                    if (timeTaken < 0.5)
                        bufferSize = Integer.min(
                            bufferSize * SPEED_MULTIPLICATION_FACTOR,
                            MAX_SPEED_TESTING_BYTES
                        )
                    val newSpeed = bufferSize / (timeTaken * 1024)
                    if (newSpeed < POSITIVE_INFINITY)
                        avgSpeedWindow.add(newSpeed)
                    start = System.currentTimeMillis()
                }
                if (avgSpeedWindow.size > 1)
                    emitter.onSuccess(avgSpeedWindow.sum() / avgSpeedWindow.size)
                else
                    emitter.onSuccess(POSITIVE_INFINITY)
            }
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
