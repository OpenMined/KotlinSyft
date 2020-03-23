package org.openmined.syft.domain

import org.openmined.syft.datasource.ModuleDataSource
import org.pytorch.Module

class ModelRepository constructor(
    private val moduleDataSource: ModuleDataSource
) {
    fun loadModule(modelName: String): Module {
        return moduleDataSource.loadModule(modelName)
    }
}
