package org.openmined.syft


import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import kotlinx.serialization.Serializable
import org.openmined.syft.network.NetworkMessage
import org.openmined.syft.threading.ProcessSchedulers

@Serializable
class Job(val modelName: String, val version: String) {

    /**
     * create a worker job (throw warning if more than 1 job are created)
     * notify PyGrid that worker is available for cycle
     * send info like average up/down speed, ping, torchscript is needed
     */
    fun start(){

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