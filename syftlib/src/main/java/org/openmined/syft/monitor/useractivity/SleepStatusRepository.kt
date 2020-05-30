package org.openmined.syft.monitor.useractivity

import android.app.ActivityManager
import io.reactivex.Flowable
import org.openmined.syft.monitor.BroadCastListener
import org.openmined.syft.monitor.StateChangeMessage

class SleepStatusRepository(
    private val activityStatus: ActivityManager
) : BroadCastListener{

    override fun subscribeStateChange(): Flowable<StateChangeMessage> {
        TODO("Not yet implemented")
    }

    override fun unsubscribeStateChange() {
        TODO("Not yet implemented")
    }
}