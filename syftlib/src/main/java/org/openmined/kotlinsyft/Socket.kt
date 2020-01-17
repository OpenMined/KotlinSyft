package org.openmined.kotlinsyft

import android.os.Message
import java.net.Socket
import java.util.logging.Logger

class Socket(url:String,logger: Logger,workerId:String,onOpen: Boolean,onClose:Boolean,onMessage: Message,keepAliveTimeout: Int){
    //TODO add port config
    val socket = Socket(url,8888)

    //TODO get protobuf Promise tensor class
    fun send(type:String,data:String){

    }
}