package org.openmined.syft.domain

import org.pytorch.IValue

data class TrainingParameters(val inputParams: List<PlanInputSpec>)

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

enum class OutputParamType {
    Loss,
    Metric,
    ModelParameter
}
