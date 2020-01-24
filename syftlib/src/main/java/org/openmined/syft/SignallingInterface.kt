package org.openmined.syft

import io.reactivex.Scheduler

interface SignallingInterface {

    /**
     * computeThreadScheduler defines the thread on which observable runs
     * @sample computeThreadScheduler Schedulers.io()
     */
    val computeThreadScheduler : Scheduler

    /**
     * calleeThreadScheduler defines the thread on which the callback to observable is run
     * @sample calleeThreadScheduler AndroidSchedulers.MainThread()
     */
    val calleeThreadScheduler : Scheduler

    /**
     * Called in the UI activity as a response to message received from PyGrid
     * Runs on the thread generated by calleeThreadScheduler
     * @param message : message to be sent
     */
    fun onMessage(message: String)

    /**
     * Called in the UI activity when connection to PyGrid closes
     * Runs on the thread generated by calleeThreadScheduler
     */
    fun onClose()

    /**
     * Called in the UI activity when connection to PyGrid is established
     * Runs on the thread generated by calleeThreadScheduler
     */
    fun onOpen()
}