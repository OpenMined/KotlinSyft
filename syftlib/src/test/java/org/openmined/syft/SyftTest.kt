package org.openmined.syft

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.jupiter.api.Test
import org.openmined.syft.networking.clients.NetworkMessage
import org.openmined.syft.networking.clients.SocketSignallingClient
import org.openmined.syft.threading.ProcessSchedulers

internal class SyftTest {

    @Test
    @ExperimentalUnsignedTypes
    fun `Given a syft object when start is invoked the the signalling client is started`() {
        val signallingClient = mock<SocketSignallingClient>()
        whenever(signallingClient.start()).thenReturn(Flowable.just(NetworkMessage.SocketOpen))
        val schedulers = mock<ProcessSchedulers> {
            on { computeThreadScheduler } doReturn Schedulers.trampoline()
            on { calleeThreadScheduler } doReturn Schedulers.trampoline()
        }

        Syft.getInstance(signallingClient, schedulers)
        verify(signallingClient).start()
    }
}
