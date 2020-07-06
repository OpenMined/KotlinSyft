package org.openmined.syft.unit.domain

import android.content.Context
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.openmined.syft.domain.SyftConfiguration

@ExperimentalUnsignedTypes
class SyftConfigurationTest {

    @Mock
    private lateinit var context: Context

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        whenever(context.filesDir).doReturn(mock())
    }

    @Test
    fun `SyftConfiguration has monitor device true by default`() {
        val config = SyftConfiguration.builder(context, "test").build()
        assert(config.monitorDevice)
    }

    @Test
    fun `When enableBackgroundExecution is called SyftConfiguration has monitor device is set to false`() {
        val config = SyftConfiguration.builder(context, "test")
                .enableBackgroundServiceExecution()
                .build()
        assert(!config.monitorDevice)
    }
}