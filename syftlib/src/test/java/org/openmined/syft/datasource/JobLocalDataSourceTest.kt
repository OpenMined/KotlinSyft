package org.openmined.syft.datasource

import com.google.protobuf.ByteString
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.openmined.syftproto.execution.v1.PlanOuterClass

class JobLocalDataSourceTest {

    @Rule
    @JvmField
    var tempFolder = TemporaryFolder()

    @Test
    fun `Given an input stream when save is invoked then file is created and absolute path is returned`() {
        val cut = JobLocalDataSource()
        val inputStream = "Hello".byteInputStream()
        // fun save(input: InputStream, dir: String, fileName: String): Single<String> {
        val file = tempFolder.newFile("myModel.pb")
        val result = cut.save(inputStream, file).test()

        result.assertNoErrors()
                .assertComplete()
        val value = result.values()[0]
        assert(value.endsWith("myModel.pb"))
        assert(file.readBytes().isNotEmpty())
    }

    @Test
    fun `Given a Torchscript plan when saveTorchscript is invoked it is saved`() {
        val cut = JobLocalDataSource()
        val file = tempFolder.newFile("torchScript.pt")
        val byteArray = "Something".toByteArray()
        val torchScriptByteString = mock<ByteString> {
            on { toByteArray() } doReturn byteArray
        }
        val plan = mock<PlanOuterClass.Plan> {
            on { torchscript } doReturn torchScriptByteString
        }

        val result =    cut.saveTorchScript(file, plan)

        assert(result.endsWith("torchScript.pt"))
        assert(file.readBytes().isNotEmpty())

    }
}