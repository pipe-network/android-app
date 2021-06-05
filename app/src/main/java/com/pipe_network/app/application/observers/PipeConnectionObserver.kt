package com.pipe_network.app.application.observers

import org.saltyrtc.client.events.SignalingStateChangedEvent
import org.webrtc.DataChannel
import org.webrtc.PeerConnection

interface PipeConnectionObserver {
    fun onSignalingStateChanged(signalingStateChangedEvent: SignalingStateChangedEvent)
    fun onPeerConnectionSignalingStateChanged(peerConnectionSignalingState: PeerConnection.SignalingState)
    fun onHandover()
    fun onDataChannelContextMessage(byteArray: ByteArray)
    fun onDataChannelContextStateChange(state: DataChannel.State)
}