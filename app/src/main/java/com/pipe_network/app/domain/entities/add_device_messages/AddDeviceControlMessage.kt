package com.pipe_network.app.domain.entities.add_device_messages

data class AddDeviceControlMessage(
    val type: String = "add_device_control_message",
    val uuid: String,
)
