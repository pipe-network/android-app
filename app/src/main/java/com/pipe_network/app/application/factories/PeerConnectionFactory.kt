package com.pipe_network.app.application.factories

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.pipe_network.app.application.handlers.PeerConnectionHandler
import com.pipe_network.app.application.observers.PipeConnectionObserver
import org.saltyrtc.client.exceptions.ConnectionException
import org.saltyrtc.tasks.webrtc.WebRTCTask
import org.saltyrtc.tasks.webrtc.messages.Candidate
import org.saltyrtc.tasks.webrtc.messages.Offer
import org.webrtc.*
import org.webrtc.PeerConnectionFactory
import java.lang.Exception

class PeerConnectionFactory {
    companion object {
        private fun sendIceCandidateToRemotePeer(task: WebRTCTask, iceCandidate: IceCandidate) {
            val candidate =
                Candidate(iceCandidate.sdp, iceCandidate.sdpMid, iceCandidate.sdpMLineIndex)
            try {
                task.sendCandidates(arrayOf(candidate))
            } catch (error: ConnectionException) {
                Log.e(PeerConnectionHandler.TAG, "Could not send ICE candidate", error)
            }
        }

        private fun sendOffer(peerConnection: PeerConnection, task: WebRTCTask) {
            Log.d(PeerConnectionHandler.TAG, "Create offer")
            lateinit var offerDescription: SessionDescription
            peerConnection.createOffer(
                object : SdpObserver {
                    override fun onCreateSuccess(sessionDescription: SessionDescription) {
                        Log.d(PeerConnectionHandler.TAG, "Offer could be set successfully created")
                        offerDescription = sessionDescription
                        Log.d(PeerConnectionHandler.TAG, "Set offer to localDescription")
                        peerConnection.setLocalDescription(this, sessionDescription)
                    }

                    override fun onSetSuccess() {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "Offer could be set successfully to localDescription"
                        )
                        val offer = Offer(offerDescription.description)

                        try {
                            task.sendOffer(offer)
                            Log.d(PeerConnectionHandler.TAG, "Sent offer: " + offer.sdp)
                        } catch (error: ConnectionException) {
                            Log.e(
                                PeerConnectionHandler.TAG,
                                "Could not send offer: " + error.message
                            )
                        }
                    }

                    override fun onCreateFailure(failure: String) {
                        Log.e(PeerConnectionHandler.TAG, "create offer create failure: $failure")
                    }

                    override fun onSetFailure(failure: String) {
                        Log.e(PeerConnectionHandler.TAG, "create offer set failure: $failure")
                    }
                },
                MediaConstraints(),
            )
        }

        private fun peerConnectionFactory(
            context: Context,
        ): PeerConnectionFactory {
            PeerConnectionFactory.initialize(
                PeerConnectionFactory
                    .InitializationOptions
                    .builder(context)
                    .setEnableInternalTracer(true)
                    .createInitializationOptions()
            )

            return PeerConnectionFactory.builder().createPeerConnectionFactory()
        }

        fun createAsInitiator(
            context: Context,
            task: WebRTCTask,
            pipeConnectionObserver: PipeConnectionObserver,
        ): PeerConnection {
            lateinit var peerConnection: PeerConnection
            val peerConnectionFactory = peerConnectionFactory(context)
            peerConnection = peerConnectionFactory.createPeerConnection(
                IceServerFactory.createDefaultIceServers(),
                object : PeerConnection.Observer {
                    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "SignalingConnection state change: " + signalingState.name
                        )
                        pipeConnectionObserver.onPeerConnectionSignalingStateChanged(signalingState)
                    }

                    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "ICE connection change to " + iceConnectionState.name
                        )
                    }

                    override fun onIceConnectionReceivingChange(receiving: Boolean) {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "ICE connection receiving change: $receiving"
                        )
                    }

                    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "ICE gathering change: " + iceGatheringState.name
                        )
                    }

                    override fun onIceCandidate(iceCandidate: IceCandidate) {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "ICE candidate gathered: " + iceCandidate.sdp
                        )

                        // Send candidate to the remote peer
                        sendIceCandidateToRemotePeer(task, iceCandidate)
                    }

                    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
                        Log.d(PeerConnectionHandler.TAG, "ICE candidates removed")
                    }

                    override fun onDataChannel(dc: DataChannel) {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "New data channel was created: " + dc.label()
                        )
                    }

                    override fun onRenegotiationNeeded() {
                        Log.d(PeerConnectionHandler.TAG, "Negotiation needed")
                        sendOffer(peerConnection, task)
                    }

                    override fun onAddStream(mediaStream: MediaStream) {
                        Log.w(PeerConnectionHandler.TAG, "Stream added")
                    }

                    override fun onRemoveStream(mediaStream: MediaStream) {
                        Log.w(PeerConnectionHandler.TAG, "Stream removed")
                    }

                    override fun onAddTrack(
                        rtpReceiver: RtpReceiver,
                        mediaStreams: Array<MediaStream>
                    ) {
                        Log.w(PeerConnectionHandler.TAG, "Add track")
                    }
                },
            ) ?: throw Exception("Could not create a PeerConnection as initiator")
            return peerConnection
        }

        fun createAsResponder(
            context: Context,
            task: WebRTCTask,
            pipeConnectionObserver: PipeConnectionObserver,
            onDataChannel: (dataChannel: DataChannel) -> Unit,
        ): PeerConnection {
            lateinit var peerConnection: PeerConnection
            val peerConnectionFactory = peerConnectionFactory(context)

            peerConnection = peerConnectionFactory.createPeerConnection(
                IceServerFactory.createDefaultIceServers(),
                object : PeerConnection.Observer {
                    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "SignalingConnection state change: " + signalingState.name
                        )
                        pipeConnectionObserver.onPeerConnectionSignalingStateChanged(signalingState)
                    }

                    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "ICE connection change to " + iceConnectionState.name
                        )
                    }

                    override fun onIceConnectionReceivingChange(receiving: Boolean) {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "ICE connection receiving change: $receiving"
                        )
                    }

                    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "ICE gathering change: " + iceGatheringState.name
                        )
                    }

                    override fun onIceCandidate(iceCandidate: IceCandidate) {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "ICE candidate gathered: " + iceCandidate.sdp
                        )

                        // Send candidate to the remote peer
                        sendIceCandidateToRemotePeer(task, iceCandidate)
                    }

                    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
                        Log.d(PeerConnectionHandler.TAG, "ICE candidates removed")
                    }

                    @RequiresApi(Build.VERSION_CODES.N)
                    override fun onDataChannel(dataChannel: DataChannel) {
                        Log.d(
                            PeerConnectionHandler.TAG,
                            "New data channel was created: " + dataChannel.label()
                        )
                        onDataChannel(dataChannel)
                    }

                    override fun onRenegotiationNeeded() {
                        Log.d(PeerConnectionHandler.TAG, "Negotiation needed")
                    }

                    override fun onAddStream(mediaStream: MediaStream) {
                        Log.w(PeerConnectionHandler.TAG, "Stream added")
                    }

                    override fun onRemoveStream(mediaStream: MediaStream) {
                        Log.w(PeerConnectionHandler.TAG, "Stream removed")
                    }

                    override fun onAddTrack(
                        rtpReceiver: RtpReceiver,
                        mediaStreams: Array<MediaStream>
                    ) {
                        Log.w(PeerConnectionHandler.TAG, "Add track")
                    }
                },
            ) ?: throw Exception("Could not create a PeerConnection as responder")
            return peerConnection
        }
    }
}