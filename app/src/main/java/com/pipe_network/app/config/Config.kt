package com.pipe_network.app.config

class Config {
    companion object {
        const val STUN_SERVER = "stun.l.google.com:19302"
        val TURN_SERVER = null;
        val TURN_USER = null;
        val TURN_PASS = null;
        const val DC_LABEL = "much-secure";
        const val SERVER_PUBLIC_KEY = "4d1167c44d98536c8693c26e4bffae20ff440a2535cab6418b2f177b4703de08"
        const val SERVER_ADDRESS_HOST = "192.168.0.69"
        const val SERVER_ADDRESS_PORT = 8080
        const val WEBSOCKET_PING_INTERVAL = 60
        const val WEBSOCKET_CONNECTION_TIMEOUT = 15 * 1000
        const val WEBSOCKET_ATTEMPTS_MAX = 5
    }
}