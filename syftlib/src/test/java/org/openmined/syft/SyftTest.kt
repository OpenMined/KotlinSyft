package org.openmined.syft

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.openmined.syft.threading.ProcessSchedulers


internal class SyftTest {

    @Test
    fun `Given params are all correct then constructor will return a valid Syft object`() {
        val workerId = "workerId"
        val keepAliveTimeout = 20000
        val url = "wss://someAddress"
        val schedulers = mock<ProcessSchedulers>()

        val cut = Syft(workerId, keepAliveTimeout, url, schedulers)

        assert(cut != null)
    }

    @Test
    fun `Given socket protocol is not wss then constructor will return a null object`() {
        val workerId = "workerId"
        val keepAliveTimeout = 20000
        val url = "ws://someAddress"
        val schedulers = mock<ProcessSchedulers>()

        val cut = Syft(workerId, keepAliveTimeout, url, schedulers)

        assert(cut == null)
    }

    @Test
    fun `Given keep alive protocol is negative then constructor will return a null object`() {
        val workerId = "workerId"
        val keepAliveTimeout = -10
        val url = "wss://someAddress"
        val schedulers = mock<ProcessSchedulers>()

        val cut = Syft(workerId, keepAliveTimeout, url, schedulers)

        assert(cut == null)
    }
}
