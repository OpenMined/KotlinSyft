package org.openmined.kotlinsyft

import android.util.Log
import org.openmined.kotlinsyft.Constants.WEBRTC_JOIN_ROOM
import org.webrtc.*
import org.webrtc.PeerConnection.*
import java.nio.ByteBuffer

typealias SDP_type = SessionDescription.Type

private const val TAG = "WebRTCClient"

open class WebRTCClient(
    peerOptions: PeerConnectionFactory.Options,
    private val peerConfig: RTCConfiguration, private val socket: Socket){

    private val peerConnectionFactory = PeerConnectionFactory.builder()
                                        .setOptions(peerOptions)
                                        .createPeerConnectionFactory()

    var peers = HashMap<String,Peer>()
    lateinit var workerId:String
    lateinit var scopeId:String

    fun start(workerId:String,scopeId:String){
        Log.d(TAG,"Joining room $scopeId")

        this.workerId = workerId
        this.scopeId = scopeId
        //TODO send as json {workerID,scopeId}
        socket.send(WEBRTC_JOIN_ROOM,workerId)
    }

    fun createConnection(worker_id: String){
        Log.d(TAG,"Creating Connection")
        val pc = peerConnectionFactory.createPeerConnection( peerConfig,
            PeerConnectionObserver(worker_id,SDP_type.ANSWER))
        peers[worker_id] = Peer(pc,null)
    }

    fun stop(){
        Log.d(TAG,"WebRTC disconnecting from peers")
        peers.forEach { (worker_id,peer)->
            if(peer.channel != null)
                removePeer(worker_id)
        }
    }

    private fun removePeer(worker_id: String){
        if(!peers.keys.contains(worker_id)) return

        Log.d(TAG,"`Closing connection to $worker_id")
        try {
            peers[worker_id]!!.channel?.close()
            //uncomment this if we need to close the connection as well
//            peers[workerId]!!.connection.close()
        }catch (e:Exception){
            Log.e(TAG,"error removing peer $worker_id",e)
        }
        peers.remove(worker_id)
    }

    fun sendMessage(message:String,to:String?){
        Log.d(TAG,"sending message $message")

        if (to!=null && !to.contentEquals(workerId) && peers[to]?.channel!=null)
            send(peers[to]!!.channel!!,message)
        else
            peers.filterValues { it.channel!=null }
                .forEach {(_, peer) ->send(peer.channel!!,message)}
    }

    private fun send(channel: DataChannel,msg:String){
        try {
            channel.send(DataChannel.Buffer(ByteBuffer.wrap(msg.toByteArray()),false))
        }catch (e:Exception){
            Log.e(TAG,"error sending message",e)
        }
    }

    private fun sendInternalMessage(type:String,message: String?,to: String){
        //TODO implement this
    }

    fun recieveNewPeer(worker_id: String){
        Log.d(TAG,"Adding new peer")
        val pc = peerConnectionFactory.createPeerConnection(peerConfig,
            PeerConnectionObserver(worker_id,SDP_type.OFFER))
        peers[worker_id] = Peer(pc,null)
        // add DataChannel constraints in init if needed. Currently default initialization
        peers[worker_id]?.apply {
            channel = pc?.createDataChannel("dataChannel",DataChannel.Init())
            channel?.registerObserver(DataChannelObserver(channel))
        }
        TODO("create offer via sdp observer")
    }

    private class SDPObserver:SdpObserver{
        override fun onSetFailure(p0: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSetSuccess() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onCreateSuccess(p0: SessionDescription?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onCreateFailure(p0: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    private inner class PeerConnectionObserver (private val worker_id: String,
                                                private val sdpType: SDP_type): Observer{

        override fun onIceCandidate(new_candidate: IceCandidate) {
            Log.d(TAG,"Saving new ICE Candidate")
            peers[worker_id]?.iceCandidates?.add(new_candidate)
        }

        override fun onDataChannel(dc: DataChannel) {
            Log.d(TAG,"Calling onDataChannel ${dc.label()}")
            dc.registerObserver(DataChannelObserver(dc))
            peers[worker_id]?.channel = dc

        }

        override fun onIceConnectionReceivingChange(p0: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onIceConnectionChange(p0: IceConnectionState?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onIceGatheringChange(gatheringState: IceGatheringState) {
            // Check if discovery is complete and send sdp answer
            if (gatheringState == IceGatheringState.COMPLETE){
                Log.d(TAG,"sending ${sdpType.canonicalForm()} and stored ICE candidates")
                sendInternalMessage(sdpType.canonicalForm(),
                    peers[worker_id]?.connection?.localDescription?.description,
                    worker_id)

                peers[worker_id]?.iceCandidates?.forEach{sendInternalMessage("candidate",it.sdp,worker_id)}

            }


        }

        override fun onSignalingChange(signalingState: SignalingState) {
            Log.d(TAG,"Signalling State: $signalingState")
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onRenegotiationNeeded() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
        override fun onRemoveStream(p0: MediaStream?) {}
        override fun onAddStream(p0: MediaStream?) {}

    }

    private class DataChannelObserver(val dataChannel: DataChannel?):DataChannel.Observer{
        override fun onMessage(buffer: DataChannel.Buffer) {
            if (buffer.binary){
                Log.d(TAG,"Data channel received binary message at $dataChannel")
                return
            }

            Log.d(TAG,"Data channel message ${buffer.data}")
        }

        override fun onBufferedAmountChange(previous_amount: Long) {
            Log.d(TAG,"buffered amount changed from $previous_amount to ${dataChannel?.bufferedAmount()}")
        }

        override fun onStateChange() {
            Log.d(TAG,"Data channel state changed to ${dataChannel?.state()}")
        }

    }

    class Peer(var connection:PeerConnection?,var channel: DataChannel?){
        val iceCandidates = ArrayList<IceCandidate>()
    }
}