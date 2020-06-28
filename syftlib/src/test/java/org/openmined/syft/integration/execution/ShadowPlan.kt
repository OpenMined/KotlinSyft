package org.openmined.syft.integration.execution

import org.openmined.syft.execution.Plan
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import org.openmined.syftproto.execution.v1.PlanOuterClass
import org.pytorch.IValue
import org.pytorch.Module
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.io.File

@ExperimentalUnsignedTypes
@Implements(Plan::class)
class ShadowPlan {

    @Implementation
    fun execute(
        model: SyftModel,
        trainingBatch: Pair<IValue, IValue>,
        clientConfig: ClientConfig
    ): IValue? {
        return null
    }

    @Implementation
    fun loadScriptModule(torchScriptLocation: String) = Unit
}