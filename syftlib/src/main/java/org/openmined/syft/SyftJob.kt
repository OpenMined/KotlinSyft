package org.openmined.syft


import kotlinx.serialization.Serializable

@Serializable
class SyftJob(val modelName: String, val version: String? = null) {

    /**
     * create a worker job
     */
    fun start() {

    }

    /**
     * Run this once the PyGrid accepts the worker
     * all the requisite plans, protocols are downloaded
     */
    fun executeTrainingPlan() {

    }

    /**
     * if not empty execute protocol after training
     */
    fun executeProtocol() {

    }

    /**
     * report the results back to PyGrid
     */
    fun report() {

    }

}