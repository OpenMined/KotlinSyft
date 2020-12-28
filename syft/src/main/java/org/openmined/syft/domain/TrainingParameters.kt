package org.openmined.syft.domain

import org.pytorch.IValue

data class TrainingParameters(val inputParams: List<PlanInputSpec>, val outputParams: List<PlanOutputSpec>)

data class PlanInputSpec(
    val type: InputParamType,
    val index: Int? = null,
    val name: String? = null,
    val value: IValue? = null
)

enum class InputParamType {
    Data,
    Target,
    ModelParameter,
    Value
}

data class PlanOutputSpec(
    val type: OutputParamType,
    val name: String? = null
)

enum class OutputParamType {
    Loss,
    Metric,
    ModelParameter
}
