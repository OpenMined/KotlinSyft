package org.openmined.syft.unit.datasource

import android.content.Context
import android.content.res.AssetManager
import com.google.protobuf.ByteString
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.openmined.syft.datasource.DIFF_SCRIPT_NAME
import org.openmined.syft.datasource.JobLocalDataSource
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syftproto.execution.v1.PlanOuterClass
import java.io.File
import java.io.InputStream

class JobLocalDataSourceTest {

    @Rule
    @JvmField
    var tempFolder = TemporaryFolder()

    private lateinit var cut: JobLocalDataSource

    @ExperimentalUnsignedTypes
    private val config: SyftConfiguration = mockk()

    @ExperimentalUnsignedTypes
    @Before
    fun setUp() {
        every { config.filesDir } answers { File("/filesDir") }
        cut = JobLocalDataSource(config)
    }

    @ExperimentalUnsignedTypes
    @Test
    fun `Given config getDiffScript returns correct asset stream`() {
        val context = mockk<Context>()

        val inputStream = mockk<InputStream>()
        val assets = mockk<AssetManager>(){
            every { open("torchscripts/$DIFF_SCRIPT_NAME") } returns inputStream
        }

        every { config.context } answers { context }
        every { context.assets } answers { assets }

        cut.getDiffScript()

        io.mockk.verify {
            assets.open("torchscripts/$DIFF_SCRIPT_NAME")
        }
    }

    @Test
    fun `save calls internal save and creates directory when parent directory exists`(){
        val parent = tempFolder.newFolder("filesDir","parent")
        val inputStream = "Hello".byteInputStream()
        val file = File(parent,"myModel.pb")
        val result = cut.save(inputStream, parent.toString(),file.name, true)
        assert(result.endsWith("myModel.pb"))
        assert(file.readText() == "Hello")
    }

    @Test
    fun `save calls internal save and creates directory when parent directory doesn't exist`(){
        val filesDir = tempFolder.newFolder("filesDir")
        val parent = File(filesDir,"parent")
        val inputStream = "Hello".byteInputStream()
        val file = File(parent,"myModel.pb")
        val result = cut.save(inputStream, parent.toString(),file.name, true)
        assert(result.endsWith("myModel.pb"))
        assert(file.readText() == "Hello")
    }

    @Test
    fun `Given an input stream when save is invoked then absolute path is returned directly if file exists and overwrite is false`() {
        val inputStream = "Hello".byteInputStream()
        val file = tempFolder.newFile("myModel.pb")
        val result = cut.save(inputStream, file, false)
        assert(result.endsWith("myModel.pb"))
        assert(file.readBytes().isEmpty())
    }

    @Test
    fun `Given an input stream when save is invoked then file is created and absolute path is returned`() {
        val inputStream = "Hello".byteInputStream()
        val file = tempFolder.newFile("myModel.pb")
        val result = cut.save(inputStream, file,true)
        assert(result.endsWith("myModel.pb"))
        assert(file.readText() == "Hello")
    }

    @Test
    fun `Given an input stream when saveAsync is invoked then absolute path is returned directly if file exists and overwrite is false`() {
        val inputStream = "Hello".byteInputStream()
        val file = tempFolder.newFile("myModel.pb")
        val result = cut.saveAsync(inputStream, file,false)

        assert(result.endsWith("myModel.pb"))
        assert(file.readBytes().isEmpty())
    }

    @Test
    fun `Given an input stream when saveAsync is invoked then file is created and absolute path is returned`() {
        val inputStream = "Hello".byteInputStream()
        val file = tempFolder.newFile("myModel.pb")
        val result = cut.saveAsync(inputStream, file,true)

        assert(result.endsWith("myModel.pb"))
        assert(file.readText() == "Hello")
    }

    @Test
    fun `Given a Torchscript plan when saveTorchscript is invoked it is saved`() {
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

    @ExperimentalUnsignedTypes
    @Test
    fun `get models path should return models path for a specific job identified by JobId`() {
        assert(cut.getModelsPath("1") == "${config.filesDir}/1/models")
    }

    @ExperimentalUnsignedTypes
    @Test
    fun `get plans path should return plans path for a specific job identified by JobId`() {
        assert(cut.getPlansPath("1") == "${config.filesDir}/1/plans")
    }

    @ExperimentalUnsignedTypes
    @Test
    fun `get protocols path should return protocols path for a specific job identified by JobId`() {
        assert(cut.getProtocolsPath("1") == "${config.filesDir}/1/protocols")
    }
}