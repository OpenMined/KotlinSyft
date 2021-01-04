package org.openmined.syft.data.fetchers

import org.openmined.syft.data.Dataset
import org.pytorch.IValue
import org.pytorch.Tensor

open class BaseDatasetFetcher(val dataset: Dataset) : Fetcher {

    override fun fetch(indices: List<Int>): Pair<IValue, IValue> {
        val data = arrayListOf<List<Float>>()
        val labels = arrayListOf<List<Float>>()

        var pair: Pair<Tensor, Tensor>? = null
        for (i in indices) {
            pair = dataset.getItem(i)
            data.add(pair.first.dataAsFloatArray.toList())
            labels.add(pair.second.dataAsFloatArray.toList())
        }

        val batchSize = indices.size.toLong()
        val xFeatureLength = pair!!.first.shape().last()
        val yFeatureLength = pair!!.second.shape().last()

        return Pair(
            IValue.from(Tensor.fromBlob(data.flatten().toFloatArray(), longArrayOf(batchSize, xFeatureLength))),
            IValue.from(Tensor.fromBlob(labels.flatten().toFloatArray(), longArrayOf(batchSize, yFeatureLength)))
        )
    }

}