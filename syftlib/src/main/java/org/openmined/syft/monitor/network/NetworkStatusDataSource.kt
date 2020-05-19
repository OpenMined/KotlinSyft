package org.openmined.syft.monitor.network

import android.accounts.NetworkErrorException
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.utilities.FileWriter
import org.openmined.syft.utilities.MB
import org.openmined.syft.utilities.readNBuffers
import java.util.Random


private const val SPEED_BUFFER_WINDOW = 20
private const val SPEED_MULTIPLICATION_FACTOR = 10
private const val MAX_SPEED_TESTING_BYTES = MB * 8
private const val TAG = "NetworkStateEvaluator"

@ExperimentalUnsignedTypes
class NetworkStatusRealTimeDataSource(private val configuration: SyftConfiguration) {

    private val networkManager = configuration.context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun updateNetworkValidity(
        constraints: List<Int>,
        networkStatusModel: NetworkStatusModel
    ) {
        networkManager.getNetworkCapabilities(networkManager.activeNetwork)?.let { capabilities ->
            networkStatusModel.networkValidity = constraints.all { capabilities.hasCapability(it) }
        } ?: throw Exception("Unknown network. Cannot detect network properties")
    }

    fun updatePing(workerId: String, networkStatusModel: NetworkStatusModel): Completable {
        val start = System.currentTimeMillis()
        return configuration.getDownloader().checkPing(
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
        val fileSize = 64
        val file = FileWriter(configuration.filesDir, "uploadFile")
                .writeRandomData(fileSize)
        val requestFile = file.asRequestBody("text/plain".toMediaType())
        val body = MultipartBody.Part.createFormData("sample", file.name, requestFile)
        val description = "uploadFile".toRequestBody()
        val start = System.currentTimeMillis() / 1000.0f

        return configuration.getDownloader().uploadSpeedTest(
            workerId, Random().ints(128).toString(),
            description,
            body
        )
                .flatMapCompletable { response ->
                    if (response.body()?.error == null && response.code() == 200) {
                        var speed = fileSize * 1024 / (System.currentTimeMillis() / 1000.0f - start)
                        if (speed > 100000)
                        //capping infinity
                            speed = 100000.0f
                        networkStatusModel.uploadspeed = speed.toString()
                        Log.d(TAG, "Upload Speed is ${networkStatusModel.uploadspeed} KBps")
                        file.delete()
                        Completable.complete()

                    } else {
                        file.delete()
                        Completable.error(NetworkErrorException("unable to verify upload speed"))
                    }
                }
    }

    fun updateDownloadSpeed(workerId: String, networkStatusModel: NetworkStatusModel) =
            configuration.getDownloader()
                    .downloadSpeedTest(workerId, Random().ints(128).toString())
                    .flatMap { response ->
                        evaluateDownloadSpeed(response.body())
                    }.flatMapCompletable { speed ->
                        networkStatusModel.downloadSpeed = speed.toString()
                        Log.d(TAG, "Download Speed is ${networkStatusModel.downloadSpeed} KBps")
                        Completable.complete()
                    }

    private fun evaluateDownloadSpeed(input: ResponseBody?): Single<Float> {
        if (input == null)
            return Single.error(UninitializedPropertyAccessException())

        return Single.create { emitter ->
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
                emitter.onSuccess(
                    avgSpeedWindow.sum() / Integer.min(
                        begin,
                        SPEED_BUFFER_WINDOW
                    )
                )
            }
        }
    }
}
