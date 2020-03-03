package org.openmined.syft.threading

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single

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

    fun <T> applySingleSchedulers() = { singleObservable: Single<T> ->
        singleObservable
                .subscribeOn(computeThreadScheduler)
                .observeOn(calleeThreadScheduler)
    }

    fun applyCompletableSchedulers() = { completable: Completable ->
        completable
                .subscribeOn(computeThreadScheduler)
                .observeOn(calleeThreadScheduler)
    }

    fun <T> applyFlowableSchedulers() = { flowable: Flowable<T> ->
        flowable
                .subscribeOn(computeThreadScheduler)
                .observeOn(calleeThreadScheduler)
    }
}