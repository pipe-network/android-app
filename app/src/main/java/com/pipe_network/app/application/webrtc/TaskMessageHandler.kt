package com.pipe_network.app.application.webrtc

import android.util.Log
import androidx.annotation.AnyThread
import org.saltyrtc.client.exceptions.ConnectionException
import org.saltyrtc.tasks.webrtc.WebRTCTask
import org.saltyrtc.tasks.webrtc.events.MessageHandler
import org.saltyrtc.tasks.webrtc.messages.Answer
import org.saltyrtc.tasks.webrtc.messages.Candidate
import org.saltyrtc.tasks.webrtc.messages.Offer
import org.webrtc.*

/**
 * Handler for incoming task messages.
 */
@AnyThread
class TaskMessageHandler(val peerConnection: PeerConnection, val task: WebRTCTask) :
    MessageHandler {
    override fun onOffer(offer: Offer) {
        val baseMessage = "Offer received"
        Log.d(TAG, baseMessage)

        val offerDescription = SessionDescription(SessionDescription.Type.OFFER, offer.sdp)

        // Set remote description
        peerConnection.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(description: SessionDescription) {
                Log.d(TAG, "$baseMessage:Successfully create remote description")
            }

            override fun onCreateFailure(error: String) {
                Log.d(TAG, "$baseMessage:Error to create remote description")
            }

            override fun onSetSuccess() {
                Log.d(TAG, "$baseMessage:Remote description set")
                onRemoteDescriptionSet()
            }

            override fun onSetFailure(s: String) {
                Log.e(TAG, "$baseMessage:Could not set remote description: $s")
            }
        }, offerDescription)
    }

    override fun onAnswer(answer: Answer) {
        val baseMessage = "Answer received"
        Log.d(TAG, baseMessage)
        val answerDescription = SessionDescription(SessionDescription.Type.ANSWER, answer.sdp)

        peerConnection.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(description: SessionDescription) {
                Log.d(TAG, "$baseMessage: Successfully create remote description")
            }

            override fun onCreateFailure(error: String) {
                Log.d(TAG, "$baseMessage: Error to create remote description")
            }

            override fun onSetSuccess() {
                Log.d(TAG, "$baseMessage:  Remote description set")
            }

            override fun onSetFailure(s: String) {
                Log.e(TAG, "$baseMessage: Could not set remote description: $s")
            }
        }, answerDescription)
    }

    override fun onCandidates(candidates: Array<Candidate>) {
        for (candidate: Candidate? in candidates) {
            if (candidate == null) {
                // Note: Unsure how to signal end-of-candidates to webrtc.org
                continue
            }

            val iceCandidate = IceCandidate(
                candidate.sdpMid,
                candidate.sdpMLineIndex,
                candidate.sdp,
            )

            Log.d(TAG, "New remote candidate: " + candidate.sdp)
            peerConnection.addIceCandidate(iceCandidate)
        }
    }

    private fun onRemoteDescriptionSet() {
        peerConnection.createAnswer(object : SdpObserver {
            private lateinit var answerDescription: SessionDescription
            override fun onCreateSuccess(description: SessionDescription) {
                Log.d(TAG, "Created answer")
                answerDescription = description
                peerConnection.setLocalDescription(this, description)
            }

            override fun onCreateFailure(error: String) {
                Log.e(TAG, "Could not create answer: $error")
            }

            override fun onSetSuccess() {
                Log.d(TAG, "Local description set")
                val answer = Answer(answerDescription.description)
                try {
                    task.sendAnswer(answer)
                    Log.d(TAG, "Sent answer: " + answer.sdp)
                } catch (error: ConnectionException) {
                    Log.e(TAG, "Could not send answer: " + error.message)
                }
            }

            override fun onSetFailure(error: String) {
                Log.e(TAG, "Could not set local description: $error")
            }
        }, MediaConstraints())
    }

    companion object {
        const val TAG = "TaskMessageHandler"
    }
}