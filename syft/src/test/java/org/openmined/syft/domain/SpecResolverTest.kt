package org.openmined.syft.domain

import org.junit.Test
import org.pytorch.IValue

class SpecResolverTest {

    @Test
    fun `Given a list of input parameters when the list is resolved it produces the expected result`() {
        val input = listOf(
            PlanInputSpec(InputParamType.Data),
            PlanInputSpec(InputParamType.Target),
            PlanInputSpec(InputParamType.BatchSize),
            PlanInputSpec(InputParamType.Value, name = "lr"),
            PlanInputSpec(InputParamType.ModelParameter, name = "W1", index = 0),
            PlanInputSpec(InputParamType.ModelParameter, name = "b1", index = 1),
            PlanInputSpec(InputParamType.ModelParameter, name = "W2", index = 2),
            PlanInputSpec(InputParamType.ModelParameter, name = "b1", index = 3)
        )

        val data = IValue.listFrom(1L, 2L)
        val target = IValue.listFrom(0, 1)
        val batchSize = IValue.from(64L)
        val lr = IValue.from(0.01)
        val w1 = IValue.from(0.4)
        val b1 = IValue.from(0.01)
        val w2 = IValue.from(0.2)
        val b2 = IValue.from(0.002)
        val vars = mapOf(
            InputParamType.Data to listOf(data),
            InputParamType.BatchSize to listOf(batchSize),
            InputParamType.Value to listOf(lr),
            InputParamType.Target to listOf(target),
            InputParamType.ModelParameter to listOf(w1, b1, w2, b2)
        )

        val expected = listOf(data, target, batchSize, lr, w1, b1, w2, b2)

        val result = SpecResolver.resolveInputSpec(input, vars)

        assert(expected == result)

        //       inputs: [
        //        new PlanInputSpec(PlanInputSpec.TYPE_DATA),
        //        new PlanInputSpec(PlanInputSpec.TYPE_TARGET),
        //        new PlanInputSpec(PlanInputSpec.TYPE_BATCH_SIZE),
        //        new PlanInputSpec(PlanInputSpec.TYPE_CLIENT_CONFIG_PARAM, 'lr'),
        //        new PlanInputSpec(PlanInputSpec.TYPE_MODEL_PARAM, 'W1', 0),
        //        new PlanInputSpec(PlanInputSpec.TYPE_MODEL_PARAM, 'b1', 1),
        //        new PlanInputSpec(PlanInputSpec.TYPE_MODEL_PARAM, 'W2', 2),
        //        new PlanInputSpec(PlanInputSpec.TYPE_MODEL_PARAM, 'b2', 3),
        //      ],

    }
}