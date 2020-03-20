package org.openmined.syft.demo.domain

import org.openmined.syft.demo.datasource.LocalMNISTModuleDataSource

class MNISTModuleRepository constructor(
    private val localMNISTModuleDataSource: LocalMNISTModuleDataSource
) {
    fun loadModule() = localMNISTModuleDataSource.loadModule()
}