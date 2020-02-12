package org.openmined.syft.networking.datamodels

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

const val AUTH_TYPE = "federated/authenticate"
const val CYCLE_TYPE = "federated/cycle-request"
const val CYCLE_ACCEPT = "accepted"
const val CYCLE_REJECT = "rejected"
const val REPORT_TYPE = "federated/report"


@Serializable
abstract class NetworkModels
