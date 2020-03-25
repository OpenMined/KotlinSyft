package org.openmined.syft.domain

import org.openmined.syft.datasource.ModuleDataSource
import org.pytorch.Module

interface ModelRepository {
    fun loadModule(modelName: String): Module
}

class DefaultModelRepository constructor(
    private val moduleDataSource: ModuleDataSource
) : ModelRepository {
    override fun loadModule(modelName: String): Module {
        return moduleDataSource.loadModule(modelName)
    }
}
