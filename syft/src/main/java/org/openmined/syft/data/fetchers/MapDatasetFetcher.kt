package org.openmined.syft.data.fetchers

import org.openmined.syft.data.Dataset
import org.openmined.syft.data.KTensor
import org.pytorch.IValue
import org.pytorch.Tensor

class MapDatasetFetcher(dataset: Dataset, dropLast: Boolean) :
    BaseDatasetFetcher(dataset, dropLast) {

    override fun fetch(indices: List<Int>): Pair<IValue, IValue> {
        val data = arrayListOf<List<Float>>()
        val labels = arrayListOf<List<Float>>()

        var pair: Pair<KTensor, KTensor>
        for (i in indices) {
            pair = dataset.getItem(i)
            data.add(pair.first.flattenedArray.toList())
            labels.add(pair.second.flattenedArray.toList())
        }

        val batchSize = indices.size.toLong()
        val xFeatureLength = dataset.getItem(0).first.shape.last()
        val yFeatureLength = dataset.getItem(0).second.shape.last()

        return Pair(
            IValue.from(Tensor.fromBlob(data.flatten().toFloatArray(), longArrayOf(batchSize, xFeatureLength))),
            IValue.from(Tensor.fromBlob(labels.flatten().toFloatArray(), longArrayOf(batchSize, yFeatureLength)))
        )
    }

}