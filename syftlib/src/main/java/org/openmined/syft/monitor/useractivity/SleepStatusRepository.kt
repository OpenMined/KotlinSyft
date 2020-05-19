package org.openmined.syft.monitor.useractivity

import android.app.ActivityManager
import org.openmined.syft.monitor.BroadCastListener

class SleepStatusRepository(
    private val activityStatus: ActivityManager
) : BroadCastListener{
    override fun registerListener() {
        TODO("Not yet implemented")
    }

    override fun deregisterListener() {
        TODO("Not yet implemented")
    }
}