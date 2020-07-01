package org.openmined.syft.unit.datasource

import com.google.protobuf.ByteString
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.spy
import org.openmined.syft.datasource.JobLocalDataSource
import org.openmined.syftproto.execution.v1.PlanOuterClass
import java.io.File
import java.io.InputStream

class JobLocalDataSourceTest {

    @Rule
    @JvmField
    var tempFolder = TemporaryFolder()

    @Test
    fun `Given an input stream when save is invoked then absolute path is returned directly if file exists and overwrite is false`() {
        val cut = JobLocalDataSource()
        val inputStream = "Hello".byteInputStream()
        val file = tempFolder.newFile("myModel.pb")
        val result = cut.save(inputStream, file,false)
        assert(result.endsWith("myModel.pb"))
        assert(file.readBytes().isEmpty())
    }

    @Test
    fun `Given an input stream when save is invoked then file is created and absolute path is returned`() {
        val cut = JobLocalDataSource()
        val inputStream = "Hello".byteInputStream()
        val file = tempFolder.newFile("myModel.pb")
        val result = cut.save(inputStream, file,true)
        assert(result.endsWith("myModel.pb"))
        assert(file.readText() == "Hello")
    }

    @Test
    fun `Given an input stream when saveAsync is invoked then absolute path is returned directly if file exists and overwrite is false`() {
        val cut = JobLocalDataSource()
        val inputStream = "Hello".byteInputStream()
        val file = tempFolder.newFile("myModel.pb")
        val result = cut.saveAsync(inputStream, file,false).test()

        result.assertNoErrors()
                .assertComplete()
        val value = result.values()[0]
        assert(value.endsWith("myModel.pb"))
        assert(file.readBytes().isEmpty())
    }

    @Test
    fun `Given an input stream when saveAsync is invoked then file is created and absolute path is returned`() {
        val cut = JobLocalDataSource()
        val inputStream = "Hello".byteInputStream()
        val file = tempFolder.newFile("myModel.pb")
        val result = cut.saveAsync(inputStream, file,true).test()

        result.assertNoErrors()
                .assertComplete()
        val value = result.values()[0]
        assert(value.endsWith("myModel.pb"))
        assert(file.readText() == "Hello")
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