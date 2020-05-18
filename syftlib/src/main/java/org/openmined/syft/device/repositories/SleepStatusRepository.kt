package org.openmined.syft.device.repositories

import android.app.ActivityManager
import android.content.Context

class SleepStatusRepository(context: Context) {

    private val activityStatus =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
}