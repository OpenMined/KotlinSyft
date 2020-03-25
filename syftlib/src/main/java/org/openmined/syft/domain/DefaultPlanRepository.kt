package org.openmined.syft.domain

import org.openmined.syft.datasource.PlanDataSource
import org.openmined.syft.processes.Plan
import org.pytorch.Module

interface PlanRepository {
    fun loadPlan(plan: Plan): Module
}

class DefaultPlanRepository constructor(
    private val planDataSource: PlanDataSource
) : PlanRepository {
    override fun loadPlan(plan: Plan): Module {
        return planDataSource.loadPlan("${plan.torchScriptLocation}/${plan.planId}")
    }
}
