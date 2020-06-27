package org.openmined.syft.datasource

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class JobLocalDataSourceTest {

    @Rule @JvmField var tempFolder = TemporaryFolder()

    @Test
    fun `Given an input stream when save is invoked then file is created and absoute path is returned`() {
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
}