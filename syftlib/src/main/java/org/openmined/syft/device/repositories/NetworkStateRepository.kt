package org.openmined.syft.device.repositories

import android.accounts.NetworkErrorException
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.openmined.syft.device.models.NetworkStateModel
import org.openmined.syft.networking.requests.HttpAPI
import org.openmined.syft.utilities.FileWriter
import org.openmined.syft.utilities.MB
import org.openmined.syft.utilities.readNBuffers
import java.util.Random

private const val TAG = "NetworkStateRepository"

private const val SPEED_BUFFER_WINDOW = 20
private const val SPEED_MULTIPLICATION_FACTOR = 10
private const val MAX_SPEED_TESTING_BYTES = MB * 8

//todo this downloader will actually be coming from configuration class later
class NetworkStateRepository(private val downloader: HttpAPI) {

    fun getNetworkState(workerId: String): Single<NetworkStateModel> {
        val networkState = NetworkStateModel()
        return updatePing(workerId, networkState)
                .andThen(updateDownloadSpeed(workerId, networkState))
                .andThen(updateUploadSpeed(workerId, networkState))
                .andThen(Single.just(networkState))
    }

    private fun updatePing(workerId: String, networkStateModel: NetworkStateModel): Completable {
        val start = System.currentTimeMillis()
        return downloader.checkPing(
            workerId = workerId,
            random = Random().ints(128).toString()
        )
                .flatMapCompletable { response ->
                    if (response.code() == 200 && response.body()?.error == null) {
                        networkStateModel.ping = (System.currentTimeMillis() - start).toString()
                        Log.d(TAG, "Ping is ${networkStateModel.ping} ms")
                        Completable.complete()
                    } else
                        Completable.error(NetworkErrorException("unable to get ping"))

                }
    }

    private fun updateDownloadSpeed(workerId: String, networkStateModel: NetworkStateModel) =
            downloader
                    .downloadSpeedTest(workerId, Random().ints(128).toString())
                    .flatMap { response ->
                        evaluateDownloadSpeed(response.body())
                    }.flatMapCompletable { speed ->
                        networkStateModel.downloadSpeed = speed.toString()
                        Log.d(TAG, "Download Speed is ${networkStateModel.downloadSpeed} KBps")
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
                emitter.onSuccess(avgSpeedWindow.sum() / Integer.min(begin, SPEED_BUFFER_WINDOW))
            }
        }
    }


    private fun updateUploadSpeed(
        workerId: String,
        networkStateModel: NetworkStateModel
    ): Completable {
        val fileSize = 64
        //todo change hardcoding of dest dir
        val file = FileWriter("/data/data/org.openmined.syft.demo/files", "uploadFile")
                .writeRandomData(fileSize)
        val requestFile = file.asRequestBody("text/plain".toMediaType())
        val body = MultipartBody.Part.createFormData("sample", file.name, requestFile)
        val description = "uploadFile".toRequestBody()
        val start = System.currentTimeMillis() / 1000.0f

        return downloader.uploadSpeedTest(
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
                        networkStateModel.uploadspeed = speed.toString()
                        Log.d(TAG, "Upload Speed is ${networkStateModel.uploadspeed} KBps")
                        file.delete()
                        Completable.complete()

                    } else {
                        file.delete()
                        Completable.error(NetworkErrorException("unable to verify upload speed"))
                    }
                }
    }

}