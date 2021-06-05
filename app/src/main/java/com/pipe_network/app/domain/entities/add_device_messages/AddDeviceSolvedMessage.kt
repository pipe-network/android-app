package com.pipe_network.app.domain.entities.add_device_messages

import com.fasterxml.jackson.annotation.JsonProperty

data class AddDeviceSolvedMessage(
    val type: String = "add_device_solved_message",
    val uuid: String,
    @JsonProperty("device_token") val deviceToken: String,
)
