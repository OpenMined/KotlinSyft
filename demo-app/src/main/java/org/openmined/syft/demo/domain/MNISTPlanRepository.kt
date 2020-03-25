package org.openmined.syft.demo.domain

import org.openmined.syft.demo.datasource.LocalMNISTPlanDataSource
import org.openmined.syft.domain.PlanRepository
import org.openmined.syft.processes.Plan

class MNISTPlanRepository constructor(
    private val localMNISTPlanDataSource: LocalMNISTPlanDataSource
) : PlanRepository {
    override fun loadPlan(plan: Plan) = localMNISTPlanDataSource.loadModule()
}