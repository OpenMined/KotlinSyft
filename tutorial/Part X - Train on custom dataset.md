# Part X - Train on custom dataset

A lot of effort in solving machine learning problem goes into data preparation. In this tutorial, we will show how to load and preprocess/augment data from a non trivial dataset using PyTorch tools.

To give an example of this process, let's use the default demo-app and modify [LocalMNISTDataDataSource.kt](https://github.com/OpenMined/KotlinSyft/blob/dev/demo-app/src/main/java/org/openmined/syft/demo/federated/datasource/LocalMNISTDataDataSource.kt) to load custom dataset.

The overall logic of file reading is: 
1. Read the files into buffer [(link)](#Reader-init-and-restart)
2. Filter the data with demanded scope [(link)](#Custom-data-filter)
3. Generate batches each consisting of `batchSize` of samples [(link)](#Batching-and-sampling)


Author:
- Pengyuan Zhou - GitHub: [@pengyuan-zhou](https://github.com/pengyuan-zhou)


```kotlin
package org.openmined.syft.demo.federated.datasource

import android.content.res.Resources
import org.openmined.syft.demo.R
import org.openmined.syft.demo.federated.domain.Batch
import java.io.BufferedReader
import java.io.FileReader;
import java.io.File
```

Define class and data readers.
```kotlin
class LocalMNISTDataDataSource constructor(
    private val resources: Resources
) {
    private var trainDataReader = returnDataReader()
    private var labelDataReader = returnLabelReader()
```

Define the paths to train file and label file.
If the training data and labels are in the same file, then define the paths with same value.
```kotlin
    private var trainFile = File("/path/to/train.csv")
    private var labelFile = File("/path/to/label.csv")
```


If the files locate in external storage, add following permissions to AndroidManifest.xml and read the files as follows.
```kotlin
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>  
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>  

    private var trainFile = File(Environment.getExternalStorageDirectory()+"/path/to/train.csv")
    private var labelFile = File(Environment.getExternalStorageDirectory()+"/path/to/label.csv")
```


Define the dimensions and starting indices of training data and labels, and the delimiter of the files. Change TD,LD,TS,LS according to your demand.
```kotlin
    private var trainDim = TD 
    private var labelDim = LD 
    private var trainStart = TS
    private var labelStart = LS
    private var delim = ","
```


#### Batching and sampling.
```kotlin
    fun loadDataBatch(batchSize: Int): Pair<Batch, Batch> {
        val trainInput = arrayListOf<List<Float>>()
        val labels = arrayListOf<List<Float>>()
        for (idx in 0..batchSize)
            readSample(trainInput, labels)

        val trainingData = Batch(
            trainInput.flatten().toFloatArray(),
            longArrayOf(trainInput.size.toLong(), trainDim)
        )
        val trainingLabel = Batch(
            labels.flatten().toFloatArray(),
            longArrayOf(labels.size.toLong(), labelDim)
        )
        return Pair(trainingData, trainingLabel)
    }
    // readSample reads a single line from data and label files 
    // and appends them into respective lists. These lists are 
    // then merged later to form a batch of data
    private fun readSample(
        trainInput: ArrayList<List<Float>>,
        labels: ArrayList<List<Float>>
    ) {
        val sample = readLine()

        trainInput.add(
            sample.first.map { it.trim().toFloat() }
        )
        labels.add(
            sample.second.map { it.trim().toFloat() }
        )
    }
```


#### Custom data filter.
```kotlin
    private fun readLine(): Pair<List<String>, List<String>> {
        var x = trainDataReader.readLine()?.split(delim)
        var y = labelDataReader.readLine()?.split(delim)
        if (x == null || y == null) {
            restartReader()
            x = trainDataReader.readLine()?.split(delim)
            y = labelDataReader.readLine()?.split(delim)
        }
        if (x == null || y == null)
            throw Exception("cannot read from dataset file")
        val subX = x.subList(TS, TS+TD)
        val subY = y.subList(LS, LS+LD)
        return Pair(subX, subY)
    }
```


#### Reader init and restart.
```kotlin
    private fun restartReader() {
        trainDataReader.close()
        labelDataReader.close()
        trainDataReader = returnDataReader()
        labelDataReader = returnLabelReader()
    }

    private fun returnDataReader() = BufferedReader(
        new FileReader(trainFile)
    )

    private fun returnLabelReader() = BufferedReader(
        new FileReader(labelFile)
    )

}
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
