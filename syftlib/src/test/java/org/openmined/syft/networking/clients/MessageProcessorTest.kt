package org.openmined.syft.networking.clients

import org.junit.jupiter.api.Test

internal class MessageProcessorTest {

    @Test
    fun `Given a serialized plan then message processor deserializes it`() {
        val planInputStream = javaClass.classLoader?.getResourceAsStream("proto_files/tp_ts.pb")

        val planPb = planInputStream?.buffered().use { it!!.readBytes() }

        val cut = MessageProcessor()

        val result = cut.processPlan(planPb)

        assert(result.isInitialized)
    }
}