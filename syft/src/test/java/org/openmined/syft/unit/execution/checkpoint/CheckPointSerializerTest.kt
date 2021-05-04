package org.openmined.syft.unit.execution.checkpoint

import com.nhaarman.mockitokotlin2.mock
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.openmined.syft.execution.checkpoint.CheckPoint
import org.openmined.syft.execution.checkpoint.JsonCheckPointSerializer
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.datamodels.ClientProperties

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class CheckPointSerializerTest {

    @Rule
    @JvmField
    var tempFolder = TemporaryFolder()
    
    private val checkPoint = CheckPoint(
        steps = 10,
        currentStep = 1,
        batchSize = 1,
        clientConfig = ClientConfig(
               properties = ClientProperties(
                modelName = "test",
                maxUpdates = 1,
                modelVersion = "1.0"
            ),
            planArgs = mutableMapOf<String, String>()
        )
    )
    
    private val serializer = JsonCheckPointSerializer()

    @Test
    fun `serialize and deserialize checkpoint to and from json`() {
        val serializedCheckPoint = serializer.serialize(checkPoint)

        assert(serializedCheckPoint.getInt("steps") == 10)
        assert(serializedCheckPoint.getInt("current_step") == 1)
        assert(serializedCheckPoint.getInt("batch_size") == 1)

        val cp = serializer.deserialize(serializedCheckPoint)
        assert(cp == checkPoint)
    }

    @Test
    fun `save and load checkpoint to and from disk`() {
        val file = tempFolder.newFile("ckp-1")
        val checkPoint = mock<CheckPoint>()
        val path = serializer.save(checkPoint, file.absolutePath)
        assert(file.absolutePath == path)

//        val result = serializer.load(file.absolutePath)

    }

}