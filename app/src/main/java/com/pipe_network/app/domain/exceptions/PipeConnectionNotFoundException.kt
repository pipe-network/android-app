package com.pipe_network.app.domain.exceptions

import com.pipe_network.app.infrastructure.models.Friend
import java.lang.Exception

class PipeConnectionForFriendNotFoundException(val friend: Friend) : Exception() {
    override val message: String
        get() = "Pipe Connection for friend: ${friend.publicKey} was not found"
}