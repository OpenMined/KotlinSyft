package org.openmined.syft.demo.domain

import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

class MNISTTrainer {

    fun train(
        script: Module,
        trainingSet: Pair<ArrayList<FloatArray>, ArrayList<Float>>
    ): IValue {
        val w1 = IValue.from(
            Tensor.fromBlob(
                FloatArray(392 * 784) { Random.nextFloat() / sqrt(784F) },
                longArrayOf(392, 784)
            )
        )
        val b1 = IValue.from(Tensor.fromBlob(FloatArray(392) { 0F }, longArrayOf(1, 392)))
        val w2 = IValue.from(
            Tensor.fromBlob(
                FloatArray(10 * 392) { Random.nextFloat() / sqrt(392F) },
                longArrayOf(10, 392)
            )
        )
        val b2 = IValue.from(Tensor.fromBlob(FloatArray(10) { 0F }, longArrayOf(1, 10)))

        val x = IValue.from(Tensor.fromBlob(trainingSet.first[0], longArrayOf(1, 784)))
        val y = IValue.from(Tensor.fromBlob(oneHot(trainingSet.second[0]), longArrayOf(1, 10)))
        val batchSize = IValue.from(Tensor.fromBlob(intArrayOf(32), longArrayOf(1)))
        val lr = IValue.from(Tensor.fromBlob(floatArrayOf(0.01F), longArrayOf(1)))

        val params = arrayOf(x, y, batchSize, lr, w1, b1, w2, b2)
        return script.forward(*params)
    }

    private fun oneHot(trainingSet: Float): FloatArray {
        val position = trainingSet.roundToInt()
        return FloatArray(10) { i -> if (i == position) position.toFloat() else 0F }
    }
}