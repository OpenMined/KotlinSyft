package org.openmined.syft

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.ImplicitReflectionSerializer
import org.junit.jupiter.api.Test
import org.openmined.syft.networking.clients.NetworkMessage
import org.openmined.syft.networking.clients.SignallingClient
import org.openmined.syft.threading.ProcessSchedulers

internal class SyftTest {

    @Test
    @ExperimentalUnsignedTypes
    @ImplicitReflectionSerializer
    fun `Given a syft object when start is invoked the the signalling client is started`() {
        val signallingClient = mock<SignallingClient>()
        whenever(signallingClient.start()).thenReturn(Flowable.just(NetworkMessage.SocketOpen))
        val schedulers = mock<ProcessSchedulers> {
            on { computeThreadScheduler } doReturn Schedulers.trampoline()
            on { calleeThreadScheduler } doReturn Schedulers.trampoline()
        }

        val cut = Syft(signallingClient, schedulers)

        cut.start()
        verify(signallingClient).start()
    }
}
