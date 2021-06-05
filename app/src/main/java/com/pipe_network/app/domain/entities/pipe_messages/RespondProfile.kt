package com.pipe_network.app.domain.entities.pipe_messages

import com.pipe_network.app.domain.entities.Profile

data class RespondProfile(val profile: Profile) : PipeBaseClass(TYPE) {
    companion object {
        const val TYPE = "respond_profile"
    }
}