package com.pipe_network.app.application.factories

import com.pipe_network.app.config.Config
import org.webrtc.PeerConnection

class IceServerFactory {
    companion object {
        fun createDefaultIceServers(): List<PeerConnection.IceServer> {
            val iceServers: MutableList<PeerConnection.IceServer> = arrayListOf(
                PeerConnection.IceServer.builder("stun:" + Config.STUN_SERVER).createIceServer()
            )
            if (Config.TURN_SERVER != null) {
                iceServers.add(
                    PeerConnection.IceServer.builder("turn:" + Config.TURN_SERVER)
                        .setUsername(Config.TURN_USER)
                        .setPassword(Config.TURN_PASS)
                        .createIceServer()
                )
            }
            return iceServers
        }
    }
}