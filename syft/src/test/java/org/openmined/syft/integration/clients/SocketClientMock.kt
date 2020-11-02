package org.openmined.syft.integration.clients

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.refEq
import com.nhaarman.mockitokotlin2.stub
import io.reactivex.Single
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.json
import org.openmined.syft.networking.clients.DATA
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.networking.clients.TYPE
import org.openmined.syft.networking.datamodels.SocketResponse
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.CYCLE_ACCEPT
import org.openmined.syft.networking.datamodels.syft.CYCLE_REJECT
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.networking.requests.REQUESTS
import org.robolectric.annotation.Implements

@ExperimentalUnsignedTypes
@Implements(SocketClient::class)
internal class SocketClientMock(
    private val authenticateSuccess: Boolean,
    private val cycleSuccess: Boolean
) {
    //Choosing stable kotlin serialization over default
    private val Json = Json(JsonConfiguration.Stable)
    private val mockedClient = mock<SocketClient>()
//
//    private val authenticationResponse = json {
//        TYPE to REQUESTS.AUTHENTICATION.value
//        if (authenticateSuccess)
//            DATA to json {
//                "status" to "success"
//                "worker_id" to "test_id"
//                "requires_speed_test" to true
//            }
//        else
//            DATA to json {
//                "status" to "rejected"
//                "error" to "this is auth error message"
//            }
//    }.toString()
//
//    private val cycleRequest1 = CycleRequest(
//        "test_id",
//        "test",
//        "1",
//        10,
//        1000.9f,
//        1000.0f
//    )
//
//    private val socketResponseTest1 = json {
//        TYPE to REQUESTS.CYCLE_REQUEST.value
//        DATA to if (cycleSuccess)
//            json {
//                "status" to CYCLE_ACCEPT
//                "model" to "test"
//                "version" to "1"
//                "request_key" to "random key"
//                "plans" to json {
//                    "test plan" to "1"
//                }
//                "client_config" to json {
//                    "name" to "test"
//                    "version" to "1"
//                    "batch_size" to 1L
//                    "lr" to 0.1f
//                    "max_updates" to 1
//                }
//                "protocols" to json {}
//                "model_id" to "2"
//            }
//        else
//            json {
//                "status" to CYCLE_REJECT
//                "timeout" to "never"
//            }
//    }.toString()
//
//    private val cycleRequest2 = CycleRequest(
//        "test_id",
//        "test2",
//        "1",
//        10,
//        1000.0f,
//        1000.0f
//    )
//
//    private val socketResponseTest2 = json {
//        TYPE to REQUESTS.CYCLE_REQUEST.value
//        DATA to if (cycleSuccess)
//            json {
//                "status" to CYCLE_ACCEPT
//                "model" to "test2"
//                "version" to "1"
//                "request_key" to "random key"
//                "plans" to json {
//                    "test plan" to "1"
//                }
//                "client_config" to json {
//                    "name" to "test2"
//                    "version" to "1"
//                    "batch_size" to 1L
//                    "lr" to 0.1f
//                    "max_updates" to 1
//                }
//                "protocols" to json {}
//                "model_id" to "2"
//            }
//        else
//            json {
//                "status" to CYCLE_REJECT
//                "timeout" to "never"
//            }
//    }.toString()
//
//    init {
//        mockedClient.stub {
//            on { authenticate(any()) }.thenReturn(
//                Single.just(
//                    deserializeSocket(
//                        authenticationResponse
//                    ).data as AuthenticationResponse
//                )
//            )
//
//            on {
//                getCycle(
//                    refEq(
//                        cycleRequest1,
//                        "workerId",
//                        "ping",
//                        "downloadSpeed",
//                        "uploadSpeed"
//                    )
//                )
//            }.thenReturn(
//                Single.just(deserializeSocket(socketResponseTest1).data as CycleResponseData)
//            )
//
//            on {
//                getCycle(
//                    refEq(
//                        cycleRequest2,
//                        "workerId",
//                        "ping",
//                        "downloadSpeed",
//                        "uploadSpeed"
//                    )
//                )
//            }.thenReturn(
//                Single.just(deserializeSocket(socketResponseTest2).data as CycleResponseData)
//            )
//
//            on { report(any()) }.thenReturn(
//                Single.just(ReportResponse("success"))
//            )
//
//            on { dispose() }.then {
//                // do nothing
//            }
//        }
//    }

    fun getMockedClient() = mockedClient

    private fun deserializeSocket(socketMessage: String): SocketResponse {
        return Json.parse(SocketResponse.serializer(), socketMessage)
    }
}