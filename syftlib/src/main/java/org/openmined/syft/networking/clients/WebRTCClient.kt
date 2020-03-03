package org.openmined.syft.networking.clients

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import org.openmined.syft.networking.datamodels.webRTC.InternalMessageRequest
import org.openmined.syft.networking.datamodels.webRTC.JoinRoomRequest
import org.openmined.syft.networking.datamodels.webRTC.JoinRoomResponse
import org.openmined.syft.networking.requests.WebRTCMessageTypes
import org.openmined.syft.threading.ProcessSchedulers
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.nio.ByteBuffer


typealias SDP_Type = SessionDescription.Type


private const val TAG = "WebRTCClient"

@ExperimentalUnsignedTypes
internal class WebRTCClient(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val peerConfig: PeerConnection.RTCConfiguration,
    private val socketClient: SocketClient,
    private val schedulers: ProcessSchedulers
) {

    private val peers = HashMap<String, Peer>()
    private val compositeDisposable = CompositeDisposable()
    private lateinit var workerId: String
    private lateinit var scopeId: String

    fun start(workerId: String, scopeId: String) {
        Log.d(TAG, "Joining room $scopeId")

        this.workerId = workerId
        this.scopeId = scopeId
        compositeDisposable.add(socketClient.joinRoom(JoinRoomRequest(workerId, scopeId))
                .compose(schedulers.applySingleSchedulers())
                .subscribe { _: JoinRoomResponse?, _: Throwable? -> }
        )
    }

    fun stop() {
        Log.d(TAG, "WebRTC disconnecting from peers")
        peers.forEach { (newWorkerId, peer) ->
            if (peer.channel != null)
                removePeer(newWorkerId)
        }
    }

    private fun createConnection(newWorkerId: String) {
        Log.d(TAG, "Creating Connection as answer")
        val pcObserver = PeerConnectionObserver(newWorkerId, SDP_Type.ANSWER)
        val pc = peerConnectionFactory.createPeerConnection(peerConfig, pcObserver)
        peers[newWorkerId] = Peer(
            pc,
            null,
            pcObserver,
            SDPObserver(
                newWorkerId,
                SDP_Type.ANSWER
            )
        )
    }

    private fun removePeer(newWorkerId: String) {
        if (!peers.keys.contains(newWorkerId)) return

        Log.d(TAG, "`Closing connection to $newWorkerId")
        try {
            peers[newWorkerId]!!.channel?.close()
            //uncomment this if we need to close the connection as well
            //peers[workerId]!!.connection.close()
        } catch (e: Exception) {
            Log.e(TAG, "error removing peer $newWorkerId", e)
        }
        peers.remove(newWorkerId)
    }

    /**
     * @param message : this function allows you to "broadcast" this `message` to all peers
     * @param to : you may send a targeted message to target `to`, default null
     */
    fun sendMessage(message: String, to: String? = null) {
        Log.d(TAG, "sending message $message")

        if (to != null && !to.contentEquals(workerId) && peers[to]?.channel != null)
            send(peers[to]!!.channel!!, message)
        else
            peers.filterValues { it.channel != null }
                    .forEach { (_, peer) -> send(peer.channel!!, message) }
    }

    /**
     * Send the data packets over the dataChannel without going through PyGrid
     */
    private fun send(channel: DataChannel, msg: String) {
        try {
            channel.send(DataChannel.Buffer(ByteBuffer.wrap(msg.toByteArray()), false))
        } catch (e: Exception) {
            Log.e(TAG, "error sending message", e)
        }
    }

    /**
     * Send the message via PyGrid to set up connection
     * @param type
     * @param message
     */
    private fun sendInternalMessage(type: WebRTCMessageTypes, message: String, target: String) {
        if (target != workerId) {
            Log.d(TAG, "Sending Internal WebRTC message via PyGrid")
            compositeDisposable.add(
                this.socketClient.sendInternalMessage(
                    InternalMessageRequest(
                        workerId,
                        scopeId,
                        target,
                        type.value,
                        message
                    )
                ).compose(schedulers.applySingleSchedulers()).subscribe()
            )
        }
    }

    fun receiveNewPeer(newWorkerId: String) {
        Log.d(TAG, "Adding new peer")
        val pcObserver = PeerConnectionObserver(newWorkerId, SDP_Type.OFFER)
        val pc = peerConnectionFactory.createPeerConnection(peerConfig, pcObserver)
        peers[newWorkerId] = Peer(
            pc,
            null,
            pcObserver,
            SDPObserver(
                newWorkerId,
                SDP_Type.OFFER
            )
        )
        // add DataChannel constraints in init if needed. Currently default initialization
        peers[newWorkerId]?.apply {
            channel = pc?.createDataChannel("dataChannel", DataChannel.Init())
            dataChannelObserver = DataChannelObserver(channel)
            channel?.registerObserver(dataChannelObserver)
            connection?.createOffer(sdpObserver, null)
        }
    }

    //todo shift this to reactive version
    fun receiveInternalMessage(
        types: WebRTCMessageTypes,
        newWorkerId: String,
        sessionDescription: String
    ) {

        when (types) {
            WebRTCMessageTypes.CANDIDATE -> {
                Log.d(TAG, "remote candidate received")
                if (!peers.containsKey(newWorkerId))
                    createConnection(newWorkerId)
                peers[newWorkerId]?.connection?.addIceCandidate(
                    IceCandidate(
                        null,
                        -1,
                        sessionDescription
                    )
                )
            }
            WebRTCMessageTypes.OFFER -> {
                Log.d(TAG, "remote offer received")
                if (!peers.containsKey(newWorkerId))
                    createConnection(newWorkerId)

                peers[newWorkerId]?.apply {
                    connection?.setRemoteDescription(
                        sdpObserver,
                        SessionDescription(SDP_Type.OFFER, sessionDescription)
                    )
                    connection?.createAnswer(sdpObserver, null)
                }
            }
            WebRTCMessageTypes.ANSWER -> {
                Log.d(TAG, "remote answer received")
                peers[newWorkerId]?.apply {
                    connection?.setRemoteDescription(
                        sdpObserver,
                        SessionDescription(SDP_Type.ANSWER, sessionDescription)
                    )
                }
            }
            else -> {
                Log.d(TAG, "unknown message type received")
            }
        }

    }

    inner class SDPObserver(
        private val newWorkerId: String,
        private val creatorType: SDP_Type
    ) : SdpObserver {

        override fun onSetFailure(p0: String?) {
            Log.d(TAG, "error setting description")
        }

        override fun onSetSuccess() {
            val connection = peers[newWorkerId]?.connection ?: return
            val sendIce = {
                peers[newWorkerId]!!.candidateQueue.forEach {
                    sendInternalMessage(
                        WebRTCMessageTypes.CANDIDATE,
                        it.sdp,
                        newWorkerId
                    )
                }
            }

            if (creatorType == SDP_Type.OFFER) {
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
                        WebRTCMessageTypes.OFFER,
                        connection.localDescription.description,
                        newWorkerId
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
                        WebRTCMessageTypes.ANSWER,
                        connection.localDescription.description,
                        newWorkerId
                    )
                    sendIce()
                } else {
                    // We've just set remote SDP - do nothing for now -
                    // answer will be created soon.
                    Log.d(TAG, "successfully finished setting offer as Remote description")
                }

            }
        }

        /**
         * This is called when answer or offer is created
         *  @param sessionDescription : the generated SDP
         */
        override fun onCreateSuccess(sessionDescription: SessionDescription) {
            Log.d(TAG, "created")
            if (peers[newWorkerId]?.connection != null && peers[newWorkerId]?.connection?.localDescription != null) {
                Log.e(TAG, "multiple SDP creation")
                return
            }
            peers[newWorkerId]?.connection?.setLocalDescription(this, sessionDescription)
        }

        override fun onCreateFailure(error: String?) {
            Log.e(TAG, "error creating : $error")
        }

    }

    inner class PeerConnectionObserver(
        private val newWorkerId: String,
        private val creatorType: SDP_Type
    ) : PeerConnection.Observer {

        override fun onIceCandidate(new_candidate: IceCandidate?) {
            if (new_candidate != null) {
                Log.d(TAG, "Saving new ICE Candidate")
                peers[newWorkerId]?.candidateQueue?.add(new_candidate)
            }
        }

        override fun onDataChannel(dc: DataChannel) {
            Log.d(TAG, "Calling onDataChannel ${dc.label()}")
            dc.registerObserver(DataChannelObserver(dc))
            peers[newWorkerId]?.channel = dc

        }

        override fun onIceConnectionReceivingChange(p0: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onIceGatheringChange(gatheringState: PeerConnection.IceGatheringState) {}

        override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
            Log.d(TAG, "Signalling State: $signalingState")
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onRenegotiationNeeded() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        /**
         * These methods are not used since we only exploit data channel
         */
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

    data class Peer(
        var connection: PeerConnection?,
        var channel: DataChannel?,
        val peerConnectionObserver: PeerConnectionObserver,
        val sdpObserver: SDPObserver
    ) {
        val candidateQueue = mutableListOf<IceCandidate>()
        lateinit var dataChannelObserver: DataChannelObserver
    }
}