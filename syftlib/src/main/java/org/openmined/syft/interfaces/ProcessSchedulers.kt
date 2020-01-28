package org.openmined.syft.interfaces

import io.reactivex.Scheduler

interface ProcessSchedulers {

    /**
     * computeThreadScheduler defines the thread on which observable runs
     * @sample computeThreadScheduler Schedulers.io()
     */
    val computeThreadScheduler: Scheduler

    /**
     * calleeThreadScheduler defines the thread on which the callback to observable is run
     * @sample calleeThreadScheduler AndroidSchedulers.MainThread()
     */
    val calleeThreadScheduler: Scheduler
}