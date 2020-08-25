# Part 2 - Custom image dataset loader

A lot of effort in solving machine learning problem goes into data preparation. In this tutorial, we will show how to load and preprocess/augment data from a image dataset using PyTorch tools.

To give an example of this process, let's use the default demo-app and modify [LocalMNISTDataDataSource.kt](https://github.com/OpenMined/KotlinSyft/blob/dev/demo-app/src/main/java/org/openmined/syft/demo/federated/datasource/LocalMNISTDataDataSource.kt) to load custom dataset.

The overall logic of file reading is: 
1. Read the files into buffer [(link)](#Reader-init-and-restart)
2. Filter the data with demanded scope [(link)](#Custom-data-filter)
3. Generate batches each consisting of `batchSize` of samples [(link)](#Batching-and-sampling)


```kotlin
package org.openmined.syft.demo.federated.datasource

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.databinding.library.BuildConfig
import kotlinx.io.OutputStream
import org.jetbrains.numkt.array
import org.jetbrains.numkt.core.KtNDArray
import org.jetbrains.numkt.core.reshape
import org.jetbrains.numkt.math.div
import org.jetbrains.numkt.math.minus
import org.jetbrains.numkt.math.times
import org.openmined.syft.demo.federated.domain.Batch
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.random.Random
```

Some constant properties.
```
private var DATASIZE: Int? = null
private var LABELSIZE: Int? = null
private val csvFile = "res/data/dataset.csv"
private val rootDir = "res/image/"
private val SCALESIZE = 256
private val CROPSIZE = 224
```

Define class and data readers.
```kotlin
class LocalImageDataDataSource constructor(
    private val resources: Resources
) {
    private var dataReader = returnDataReader()
```

#### Batching and sampling.
```kotlin
    fun loadDataBatch(batchSize: Int): Pair<Batch, Batch> {
        val features = arrayListOf<List<Float>>()
        val imgTensors = arrayListOf<Tensor>()
        for (idx in 0..batchSize)
          readSample(features, imgTensors)
          
        DATASIZE = features[0].size
        
        val trainingData = Batch(
            features.flatten().toFloatArray(),
            longArrayOf(features.size.toLong(), DATASIZE!!.toLong())
        )
        val trainingImage = Batch(
            imgTensors,// TODO: 25/08/2020
            longArrayOf(imgTensors.size.toLong(), LABELSIZE!!.toLong())
        )
        return Pair(trainingData, trainingImage)
    }

    private fun readSample(
        features: ArrayList<List<Float>>,
        imgTensors: ArrayList<Tensor>
    ) {
        val sample = readLine()
        var feature: KtNDArray<Float> = array(sample.drop(1).map { it.trim().toFloat() })
        // https://github.com/Kotlin/kotlin-numpy
        feature = feature.reshape(-1, 2)
        var img = File(rootDir + sample.first())

        var scaleimg = Rescale(img,feature,SCALESIZE).first
        var scalefeature = Rescale(img,feature,SCALESIZE).second
        var cropimg = RandomCrop(scaleimg,scalefeature, CROPSIZE).first
        var cropfeature = RandomCrop(img,feature,CROPSIZE).second
        val inputTensor: Tensor = TensorImageUtils.bitmapToFloat32Tensor(
            cropimg,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB
            )

        features.add(
            cropfeature
        )
        imgs.add(
            inputTensor
        )
    }
```

TODO
```
    private fun Rescale(
        img: File,
        feature: KtNDArray<Float>,
        output_size: Any
    ): Pair<File, KtNDArray<Float>> {
        if (BuildConfig.DEBUG && !(output_size is Int || output_size is Pair<*, *>)) {
            error("Assertion failed")
        }
        val bOrig: Bitmap = BitmapFactory.decodeFile(img.getAbsolutePath())
        val origHeight = bOrig.height
        val origWidth = bOrig.width
        var newHeight: Int = origHeight
        var newWidth: Int = origWidth
        if (output_size is Int) {
            if (origHeight > origWidth) {
                newHeight = output_size * origHeight / origWidth
                newWidth = output_size
            } else {
                newHeight = output_size
                newWidth = output_size * origWidth / origHeight
            }
        }
        if (output_size is Pair<*, *>) {
            newHeight = output_size.first as Int
            newWidth = output_size.second as Int
        }
        val bNew = Bitmap.createScaledBitmap(bOrig, newWidth, newHeight, false)
        val stream:OutputStream = FileOutputStream(img)
        bNew.compress(Bitmap.CompressFormat.JPEG,100,stream)
        stream.flush()
        stream.close()
        for (i in feature) {
            i[0] = i[0] * newWidth / origWidth
            i[1] = i[1] * newHeight / origHeight
        }
        return Pair(img, feature)

    }

    private fun RandomCrop(
        img: File,
        feature: KtNDArray<Float>,
        output_size: Any
    ): Pair<Bitmap, List<Float>> {

        if (BuildConfig.DEBUG && !(output_size is Int || output_size is Pair<*, *>)) {
            error("Assertion failed")
        }
        var output_sizes: Pair<Int,Int> = Pair(1,1)
        if (output_size is Int) {
            output_sizes = Pair(output_size, output_size)
        }
        if (output_size is Pair<*, *>) {
            output_sizes = output_size as Pair<Int, Int>
        }

        val bOrig: Bitmap = BitmapFactory.decodeFile(img.getAbsolutePath())
        val origHeight = bOrig.height
        val origWidth = bOrig.width
        var newHeight: Int = output_sizes.first
        var newWidth: Int = output_sizes.second

        var top: Int = Random.nextInt(0, origHeight - newHeight)
        var left: Int = Random.nextInt(0, origWidth - newWidth)
        val bNew = Bitmap.createBitmap(
            bOrig,
            left+newWidth,
            top+newHeight,
            newWidth,
            newHeight
        )

        var featurenew = feature - array(arrayOf(left.toFloat(), top.toFloat()))
        var featureNew: List<Float> = featurenew.toList()

        return Pair(bNew, featureNew)
    }


    private fun readLine(): List<String> {
        var x = dataReader.readLine()?.split(",")
        if (x == null) {
            restartReader()
            x = dataReader.readLine()?.split(",")
        }
        if (x == null)
            throw Exception("cannot read from dataset file")
        return x
    }

    private fun restartReader() {
        dataReader.close()
        dataReader = returnDataReader()
    }


    private fun returnDataReader() = BufferedReader(
        InputStreamReader(
            File(csvFile).inputStream()
        )
    )
```

# Conclusion

If you enjoyed this and would like to join the movement toward privacy preserving, decentralized ownership of AI and the AI supply chain (data), you can do so in the following ways! 

### Star KotlinSyft on GitHub

The easiest way to help our community is just by starring the repositories! This helps raise awareness of the cool tools we're building.

- [Star KotlinSyft](https://github.com/OpenMined/KotlinSyft)

### Pick our tutorials on GitHub!

We made really nice tutorials to get a better understanding of what Federated and Privacy-Preserving Learning should look like and how we are building the bricks for this to happen.

- [Checkout the KotlinSyft tutorials](https://github.com/OpenMined/KotlinSyft/tree/master/tutorial/)


### Join our Slack!

The best way to keep up to date on the latest advancements is to join our community! 

- [Join slack.openmined.org](http://slack.openmined.org)

### Join a Code Project!

The best way to contribute to our community is to become a code contributor! If you want to start "one off" mini-projects, you can go to KotlinSyft GitHub Issues page and search for issues marked `Good First Issue`.

- [Good First Issue Tickets](https://github.com/OpenMined/KotlinSyft/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22)

### Donate

If you don't have time to contribute to our codebase, but would still like to lend support, you can also become a Backer on our Open Collective. All donations go toward our web hosting and other community expenses such as hackathons and meetups!

- [Donate through OpenMined's Open Collective Page](https://opencollective.com/openmined)
