package org.openmined.syft.datasource

import io.reactivex.Single
import org.openmined.syft.utilities.FileWriter
import java.io.InputStream

internal class JobLocalDataSource {

    /**
     * Persist the given inputStream in the specified destination.
     *
     * @return The absolute path where the file has been saved if successful.
     */
    fun save(input: InputStream, dir: String, fileName: String): Single<String> {
        return FileWriter(dir, fileName).writeInputStream(input)
    }
}