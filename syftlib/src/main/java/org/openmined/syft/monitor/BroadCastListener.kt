package org.openmined.syft.monitor

import io.reactivex.Flowable

interface BroadCastListener {
    fun subscribeStateChange(): Flowable<StateChangeMessage>
    fun unsubscribeStateChange()
}