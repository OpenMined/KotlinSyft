package org.openmined.syft.monitor.useractivity

import android.app.ActivityManager
import android.content.Context

class SleepStatusRepository(context: Context) {

    private val activityStatus =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
}