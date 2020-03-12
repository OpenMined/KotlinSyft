package org.openmined.syft

import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File

class Classifier (forwardPath: String,backwardPath:String,x:String,y:String){
    var forward_model: Module = Module.load(forwardPath)
    var backward_model: Module = Module.load(backwardPath)
    val train_input = arrayListOf(FloatArray(10))
    val train_output = arrayListOf<Float>()
    init {
        File(x).forEachLine{ line ->
            train_input.add(line.split(",").map{ it.toFloat() }.toFloatArray())
        }

        File(y).forEachLine{ line ->
            train_output.add(line.toFloat())
        }
    }
    var mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    var std = floatArrayOf(0.229f, 0.224f, 0.225f)

    fun setMeanAndStd(mean: FloatArray, std: FloatArray) {

        this.mean = mean
        this.std = std
    }

    private fun preprocess(bitmap: Bitmap, size: Int): Tensor {
        var bitmap = bitmap

        bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false)
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap, this.mean, this.std)

    }

    private fun argMax(inputs: FloatArray): Int {

        var maxIndex = -1
        var maxvalue = 0.0f

        for (i in inputs.indices) {

            if (inputs[i] > maxvalue) {

                maxIndex = i
                maxvalue = inputs[i]
            }

        }


        return maxIndex
    }

    fun train() {
        var w = IValue.from(Tensor.fromBlob(FloatArray(10){1.0f}, longArrayOf(10)))
        var loss :IValue
        for (epoch in 1..500) {
            var loss_print : Float = 0.0f
            for (i in 0..500) {
                val x = Tensor.fromBlob(train_input[i], longArrayOf(10))
                val y = Tensor.fromBlob(floatArrayOf(train_output[i]), longArrayOf(1))
                loss = forward_model.forward(IValue.from(x), IValue.from(y), w)
                loss_print = loss.toTensor().dataAsFloatArray[0]
                w = backward_model.forward(w, loss, IValue.from(x))
            }
            println("loss $loss_print")
        }
    }
}