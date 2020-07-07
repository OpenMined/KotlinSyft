package org.openmined.syft.monitor

import io.reactivex.Flowable

internal interface BroadCastListener {
    fun subscribeStateChange(): Flowable<StateChangeMessage>
    fun unsubscribeStateChange()
}