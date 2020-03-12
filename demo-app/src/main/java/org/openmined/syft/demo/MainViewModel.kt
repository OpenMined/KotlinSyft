package org.openmined.syft.demo

import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.openmined.syft.networking.clients.MessageProcessor
import org.openmined.syft.threading.ProcessSchedulers
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

class MainViewModel(
    private val schedulerProvider: ProcessSchedulers,
    private val resources: Resources,
    private val filesDir: File
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    fun process(): Completable {
        return Completable.fromAction {
            val trainingSet = loadData()
            val script = loadModule()
            train(script, trainingSet)
        }.subscribeOn(schedulerProvider.computeThreadScheduler)
                .observeOn(schedulerProvider.calleeThreadScheduler)
    }

    private fun train(
        script: Module,
        trainingSet: Pair<ArrayList<FloatArray>, ArrayList<Float>>
    ): IValue {
        var w1 = IValue.from(
            Tensor.fromBlob(
                FloatArray(392 * 784) { Random.nextFloat() / sqrt(784F) },
                longArrayOf(392, 784)
            )
        )
        var b1 = IValue.from(Tensor.fromBlob(FloatArray(392) { 0F }, longArrayOf(1, 392)))
        var w2 = IValue.from(
            Tensor.fromBlob(
                FloatArray(10 * 392) { Random.nextFloat() / sqrt(392F) },
                longArrayOf(10, 392)
            )
        )
        var b2 = IValue.from(Tensor.fromBlob(FloatArray(10) { 0F }, longArrayOf(1, 10)))

        val x = IValue.from(Tensor.fromBlob(trainingSet.first[0], longArrayOf(1, 784)))
        val y = IValue.from(Tensor.fromBlob(oneHot(trainingSet.second[0]), longArrayOf(1, 10)))
        val batchSize = IValue.from(Tensor.fromBlob(intArrayOf(32), longArrayOf(1)))
        val lr = IValue.from(Tensor.fromBlob(floatArrayOf(0.01F), longArrayOf(1)))
        // self.W1 = th.randn(392, 784) / th.sqrt(th.tensor(784.))
        //        self.b1 = th.zeros(392)
        //        self.W2 = th.randn(10, 392) / th.sqrt(th.tensor(392.))
        //        self.b2 = th.zeros(10)
        // input_names = [
        //    "X", "y", "batch_size", "lr",
        //    "W1", "b1", "W2", "b2"
        //]
        Log.d("MainActivity", "x Shape ${x.toTensor().shape().asList()}")
        Log.d("MainActivity", "y Shape ${y.toTensor().shape().asList()}")
        Log.d("MainActivity", "w1 Shape ${w1.toTensor().shape().asList()}")
        Log.d("MainActivity", "b1 Shape ${b1.toTensor().shape().asList()}")
        Log.d("MainActivity", "w2 Shape ${w2.toTensor().shape().asList()}")
        Log.d("MainActivity", "b2 Shape ${b2.toTensor().shape().asList()}")
        Log.d("MainActivity", "batch size Shape ${batchSize.toTensor().shape().asList()}")
        Log.d("MainActivity", "LR Shape ${lr.toTensor().shape().asList()}")

        // The following three statements fail.
//        script.runMethod("matmul", x, w1)
//        script.runMethod("__matmul__", x, w1)
//        script.runMethod("torch.matmul", x, w1)

        return script.forward(x, y, batchSize, lr, w1, b1, w2, b2)
    }

    private fun oneHot(trainingSet: Float): FloatArray {
        val position = trainingSet.roundToInt()
        return FloatArray(10) { i -> if (i == position) position.toFloat() else 0F }
    }

    private fun loadModule(): Module {
        val module =
                MessageProcessor().processTorchScript(resources.openRawResource(R.raw.tp_ts).readBytes())
        val path = saveScript(module.obj)
        Log.d("MainActivity", "TorchScript saved at $path")
        return Module.load(path)
    }

    private fun loadData(): Pair<ArrayList<FloatArray>, ArrayList<Float>> {
        val trainInput = arrayListOf(FloatArray(784))
        val labels = arrayListOf<Float>()

        val x = resources.openRawResource(R.raw.train_small)
        BufferedReader(InputStreamReader(x))
                .forEachLine { line ->
                    trainInput.add(
                        line.split(',')
                                .map {
                                    it.trim().toFloat() / 255
                                }.toFloatArray()
                    )
                }

        val y = resources.openRawResource(R.raw.labels_small)
        BufferedReader(InputStreamReader(y))
                .forEachLine { line ->
                    labels.add(line.toFloat() / 10)
                }
        return Pair(trainInput, labels)
    }

    private fun saveScript(obj: com.google.protobuf.ByteString): String {
        val file = File(filesDir, "script")
        FileOutputStream(file).use {
            it.write(obj.toByteArray())
            it.flush()
            it.close()
        }
        return file.absolutePath
    }
}
