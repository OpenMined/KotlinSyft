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
    Epoch,
    BatchSize,
    Step,
    ModelParameter,
    Value
}

enum class OutputParamType {
    Loss,
    Metric,
    ModelParameter
}

object SpecResolver {
    fun resolveInputSpec(
        inputSpecs: List<PlanInputSpec>,
        // Note: We need this to be a List to resolve ModelParams
        vars: Map<InputParamType, List<IValue>>
    ): List<IValue> {
        return inputSpecs.fold(mutableListOf()) { args, inputSpec ->
            processInputSpec(args, inputSpec, vars)
        }
    }

    private fun processInputSpec(
        args: MutableList<IValue>,
        inputSpec: PlanInputSpec,
        vars: Map<InputParamType, List<IValue>>
    ): MutableList<IValue> {
        return when {
            inputSpec.type == InputParamType.Value -> args.apply {
                val value = vars[inputSpec.type]
                if (value != null) {
                    add(value.first())
                }
            }
//            inputSpec.index != null -> args.apply {
//                vars[inputSpec.type]?.let { add(it[inputSpec.index]) }
//            }
            else -> args.apply {
                val values = vars.getOrElse(inputSpec.type, { emptyList() })
                if (values.isNotEmpty()) add(values.first())
            }
        }
    }

    // Note: Using a MutableMap<Int, IValue> to simulate a SparseArray.
    // Otherwise, inserting a value at an arbitrary index can be a problem.
//    fun resolveOutputSpec(
//        outputSpecs: List<TrainingParameter.PlanOutputSpec>,
//        modelOutput: List<IValue>
//    ): Map<OutputParamType, MutableMap<Int, MutableList<IValue>>> {
//        return outputSpecs.foldIndexed(mutableMapOf()) { index, output, outputSpec ->
//            processOutputSpec(index, output, outputSpec, modelOutput)
//            // TODO transform to Map<OutputParamType, List<IValue>>
//        }
//    }
//
    // TODO This needs to be refactored. These data structures are too complex for what we need.
//    private fun processOutputSpec(
//        index: Int,
//        output: MutableMap<OutputParamType, MutableMap<Int, MutableList<IValue>>>,
//        outputSpec: TrainingParameter.PlanOutputSpec,
//        modelOutput: List<IValue>
//    ): MutableMap<OutputParamType, MutableMap<Int, MutableList<IValue>>> {
//        return when {
//            outputSpec.index != null -> {
//                val existingValues = (output[outputSpec.type] ?: mutableMapOf()).toMutableMap()
//                val outputValueMap = modelOutput[index]
//
//                if (existingValues.isNotEmpty()) {
//                    val idontnowhatisthisanymore = existingValues[outputSpec.index]
//                    if (idontnowhatisthisanymore.isNullOrEmpty()) {
//                        existingValues[outputSpec.index] = mutableListOf(outputValueMap)
//                    } else {
//                        idontnowhatisthisanymore.add(outputValueMap)
//                    }
//                } else {
//                    existingValues[outputSpec.index] = mutableListOf(outputValueMap)
//                }
//                output[outputSpec.type] = existingValues
//                output
//            }
//            else -> {
//                val existingValues = (output[outputSpec.type] ?: mutableMapOf()).toMutableMap()
//                val outputValueMap = modelOutput[index]
//                val maxIndex = (existingValues.keys.max() ?: -1) + 1
//
//                if (existingValues.isNotEmpty()) {
//                    val idontnowhatisthisanymore = existingValues[maxIndex]
//                    if (idontnowhatisthisanymore.isNullOrEmpty()) {
//                        existingValues[maxIndex] = mutableListOf(outputValueMap)
//                    } else {
//                        idontnowhatisthisanymore.add(outputValueMap)
//                    }
//                } else {
//                    existingValues[maxIndex] = mutableListOf(outputValueMap)
//                }
//                output[outputSpec.type] = existingValues
//                output
//            }
//        }
//    }
}
