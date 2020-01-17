package org.openmined.kotlinsyft

import android.util.Log
import org.openmined.kotlinsyft.Constants.WEBRTC_JOIN_ROOM
import org.webrtc.*
import org.webrtc.PeerConnection.*
import java.nio.ByteBuffer


class WebRTCClient(
    peerOptions: PeerConnectionFactory.Options,
    private val peerConfig: RTCConfiguration, private val socket: Socket){

    private val TAG = "WebRTCClient"
    private val peerConnectionFactory = PeerConnectionFactory.builder()
                                        .setOptions(peerOptions)
                                        .createPeerConnectionFactory()

    private var peers = HashMap<String,Peer>()
    lateinit var workerId:String
    lateinit var scopeId:String

    fun start(workerId:String,scopeId:String){
        Log.i(TAG,"WebRTC: Joining room $scopeId")
        this.workerId = workerId
        this.scopeId = scopeId
        //TODO send as json {workerID,scopeId}
        socket.send(WEBRTC_JOIN_ROOM,workerId)
    }

    fun createConnection(){

    }

    fun stop(){
        Log.i(TAG,"WebRTC disconnecting from peers")
        peers.forEach { (workerid,peer)->
            if(peer.channel != null)
                removePeer(workerid)
        }
    }

    private fun removePeer(workerId: String){
        if(!peers.keys.contains(workerId)) return

        Log.i(TAG,"`WebRTC: Closing connection to $workerId")
        try {
            peers[workerId]!!.channel!!.close()
            //TODO uncomment this if we need to close the connection as well
//            peers[workerId]!!.connection.close()
        }catch (e:Exception){
            Log.e(TAG,"WebRTC: error removing peer $workerId",e)
        }
        peers.remove(workerId)
    }

    fun sendMessage(message:String,to:String?){
        Log.i(TAG,"WebRTC: sending message $message")

        if (to!=null && !to.contentEquals(workerId) && peers[to]?.channel!=null)
            send(peers[to]!!.channel!!,message)
        else
            peers.forEach { (_, peer) ->
                if (peer.channel!=null)
                    send(peer.channel!!,message)
            }
    }

    private fun send(channel: DataChannel,msg:String){
        try {
            channel.send(DataChannel.Buffer(ByteBuffer.wrap(msg.toByteArray()),false))
        }catch (e:Exception){
            Log.e(TAG,"WebRTC: error sending message",e)
        }
    }

    private class Peer(var connection:PeerConnection,var channel: DataChannel?)

    private class Observer : PeerConnection.Observer{
        override fun onIceCandidate(p0: IceCandidate?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onDataChannel(p0: DataChannel?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onIceConnectionReceivingChange(p0: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onIceConnectionChange(p0: IceConnectionState?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onIceGatheringChange(p0: IceGatheringState?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onAddStream(p0: MediaStream?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSignalingChange(p0: SignalingState?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onRemoveStream(p0: MediaStream?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onRenegotiationNeeded() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}