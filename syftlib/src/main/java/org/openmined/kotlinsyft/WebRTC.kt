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
    private val peerConfig: RTCConfiguration, private val socket: Socket
) {

    private val peerConnectionFactory = PeerConnectionFactory.builder()
        .setOptions(peerOptions)
        .createPeerConnectionFactory()

    var peers = HashMap<String, Peer>()
    private lateinit var workerId: String
    private lateinit var scopeId: String

    fun start(workerId: String, scopeId: String) {
        Log.d(TAG, "Joining room $scopeId")

        this.workerId = workerId
        this.scopeId = scopeId
        //TODO send as json {workerID,scopeId}
        socket.send(WEBRTC_JOIN_ROOM, "{$workerId,$scopeId}")
    }

    private fun createConnection(worker_id: String) {
        Log.d(TAG, "Creating Connection as answer")
        val pcObserver = PeerConnectionObserver(worker_id, SDP_type.ANSWER)
        val pc = peerConnectionFactory.createPeerConnection(peerConfig, pcObserver)
        peers[worker_id] = Peer(pc, null, pcObserver, SDPObserver(worker_id, SDP_type.ANSWER))
    }

    fun stop() {
        Log.d(TAG, "WebRTC disconnecting from peers")
        peers.forEach { (worker_id, peer) ->
            if (peer.channel != null)
                removePeer(worker_id)
        }
    }

    private fun removePeer(worker_id: String) {
        if (!peers.keys.contains(worker_id)) return

        Log.d(TAG, "`Closing connection to $worker_id")
        try {
            peers[worker_id]!!.channel?.close()
            //uncomment this if we need to close the connection as well
            //peers[workerId]!!.connection.close()
        } catch (e: Exception) {
            Log.e(TAG, "error removing peer $worker_id", e)
        }
        peers.remove(worker_id)
    }

    // Given a message, this function allows you to "broadcast" a message to all peers
    // Alternatively, you may send a targeted message to one specific peer (specified by the "to" param)
    fun sendMessage(message: String, to: String?) {
        Log.d(TAG, "sending message $message")

        if (to != null && !to.contentEquals(workerId) && peers[to]?.channel != null)
            send(peers[to]!!.channel!!, message)
        else
            peers.filterValues { it.channel != null }
                .forEach { (_, peer) -> send(peer.channel!!, message) }
    }

    private fun send(channel: DataChannel, msg: String) {
        try {
            channel.send(DataChannel.Buffer(ByteBuffer.wrap(msg.toByteArray()), false))
        } catch (e: Exception) {
            Log.e(TAG, "error sending message", e)
        }
    }

    private fun sendInternalMessage(type: String, message: String?, to: String) {
        //TODO implement this
    }

    fun receiveNewPeer(worker_id: String) {
        Log.d(TAG, "Adding new peer")
        val pcObserver = PeerConnectionObserver(worker_id, SDP_type.OFFER)
        val pc = peerConnectionFactory.createPeerConnection(peerConfig, pcObserver)
        peers[worker_id] = Peer(pc, null, pcObserver, SDPObserver(worker_id, SDP_type.OFFER))
        // add DataChannel constraints in init if needed. Currently default initialization
        peers[worker_id]?.apply {
            channel = pc?.createDataChannel("dataChannel", DataChannel.Init())
            dataChannelObserver = DataChannelObserver(channel)
            channel?.registerObserver(dataChannelObserver)
            connection?.createOffer(sdpObserver, null)
        }
    }

    fun receiveInternalMessage(type: String, worker_id: String, sessionDescription: String) {

        when (type) {
            "candidate" -> {
                Log.d(TAG, "remote candidate received")
                if (!peers.containsKey(worker_id))
                    createConnection(worker_id)
                peers[worker_id]?.connection?.addIceCandidate(
                    IceCandidate(
                        null,
                        -1,
                        sessionDescription
                    )
                )
            }
            "offer" -> {
                Log.d(TAG, "remote offer received")
                if (!peers.containsKey(worker_id))
                    createConnection(worker_id)

                peers[worker_id]?.apply {
                    connection?.setRemoteDescription(
                        sdpObserver,
                        SessionDescription(SessionDescription.Type.OFFER, sessionDescription)
                    )
                    connection?.createAnswer(sdpObserver, null)
                }
            }
            "answer" -> {
                Log.d(TAG, "remote answer received")
                peers[worker_id]?.apply {
                    connection?.setRemoteDescription(
                        sdpObserver,
                        SessionDescription(SessionDescription.Type.ANSWER, sessionDescription)
                    )
                }
            }
        }

    }

    inner class SDPObserver(
        private val worker_id: String,
        private val creatorType: SDP_type
    ) : SdpObserver {

        override fun onSetFailure(p0: String?) {
            Log.d(TAG, "error setting description")
        }

        override fun onSetSuccess() {
            val connection = peers[worker_id]?.connection ?: return
            val sendIce = {
                peers[worker_id]!!.candidateQueue.forEach {
                    sendInternalMessage(
                        "candidate",
                        it.sdp,
                        worker_id
                    )
                }
            }

            if (creatorType == SDP_type.OFFER) {
                // For offering peer connection we first create offer and set
                // local SDP, then after receiving answer set remote SDP.
                if (connection.remoteDescription == null) {
                    // We've just set our local SDP so time to send it
                    Log.d(
                        TAG,
                        "successfully finished setting ${creatorType.canonicalForm()} as Local description"
                    )
                    Log.d(TAG, "sending ${creatorType.canonicalForm()} and stored ICE candidates")
                    sendInternalMessage(
                        creatorType.canonicalForm(),
                        connection.localDescription.description,
                        worker_id
                    )
                } else {
                    Log.d(TAG, "successfully set Remote description")
                    sendIce()
                }
            } else {
                // For answering peer connection we set remote SDP and then
                // create answer and set local SDP.
                if (connection.localDescription != null) {
                    // We've just set our local SDP so time to send answer and local ICE candidates.
                    Log.d(
                        TAG,
                        "successfully finished setting ${creatorType.canonicalForm()} as Local description"
                    )
                    Log.d(TAG, "sending ${creatorType.canonicalForm()} and stored ICE candidates")
                    sendInternalMessage(
                        creatorType.canonicalForm(),
                        connection.localDescription.description,
                        worker_id
                    )
                    sendIce()
                } else {
                    // We've just set remote SDP - do nothing for now -
                    // answer will be created soon.
                    Log.d(TAG, "successfully finished setting offer as Remote description")
                }

            }
        }

        override fun onCreateSuccess(sessionDescription: SessionDescription) {
            // This is called when answer or offer is created
            Log.d(TAG, "created")
            if (peers[worker_id]?.connection != null && peers[worker_id]?.connection?.localDescription != null) {
                Log.e(TAG, "multiple SDP creation")
                return
            }
            peers[worker_id]?.connection?.setLocalDescription(this, sessionDescription)
        }

        override fun onCreateFailure(error: String?) {
            Log.e(TAG, "error creating : $error")
        }

    }

    inner class PeerConnectionObserver(
        private val worker_id: String,
        private val creatorType: SDP_type
    ) : Observer {

        override fun onIceCandidate(new_candidate: IceCandidate?) {
            if (new_candidate != null) {
                Log.d(TAG, "Saving new ICE Candidate")
                peers[worker_id]?.candidateQueue?.add(new_candidate)
            }
        }

        override fun onDataChannel(dc: DataChannel) {
            Log.d(TAG, "Calling onDataChannel ${dc.label()}")
            dc.registerObserver(DataChannelObserver(dc))
            peers[worker_id]?.channel = dc

        }

        override fun onIceConnectionReceivingChange(p0: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onIceConnectionChange(p0: IceConnectionState?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onIceGatheringChange(gatheringState: IceGatheringState) {}

        override fun onSignalingChange(signalingState: SignalingState) {
            Log.d(TAG, "Signalling State: $signalingState")
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

    class DataChannelObserver(private val dataChannel: DataChannel?) : DataChannel.Observer {
        override fun onMessage(buffer: DataChannel.Buffer) {
            if (buffer.binary) {
                Log.d(TAG, "Data channel received binary message at $dataChannel")
                return
            }

            Log.d(TAG, "Data channel message ${buffer.data}")
        }

        override fun onBufferedAmountChange(previous_amount: Long) {
            Log.d(
                TAG,
                "buffered amount changed from $previous_amount to ${dataChannel?.bufferedAmount()}"
            )
        }

        override fun onStateChange() {
            Log.d(TAG, "Data channel state changed to ${dataChannel?.state()}")
        }

    }

    class Peer(
        var connection: PeerConnection?,
        var channel: DataChannel?,
        val peerConnectionObserver: PeerConnectionObserver,
        val sdpObserver: SDPObserver
    ) {
        val candidateQueue = ArrayList<IceCandidate>()
        lateinit var dataChannelObserver: DataChannelObserver
    }
}