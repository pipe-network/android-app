package com.pipe_network.app.application.repositories

import com.pipe_network.app.domain.exceptions.PipeConnectionForFriendNotFoundException
import com.pipe_network.app.domain.models.PipeConnection
import com.pipe_network.app.infrastructure.models.Friend

interface PipeConnectionFriendRepository {
    fun addPipeConnectionByFriend(friend: Friend, pipeConnection: PipeConnection)
    fun getPipeConnectionByFriend(friend: Friend): PipeConnection
}

class PipeConnectionFriendRepositoryImpl : PipeConnectionFriendRepository {
    private val values: MutableMap<Friend, PipeConnection> = HashMap()

    override fun addPipeConnectionByFriend(friend: Friend, pipeConnection: PipeConnection) {
        values[friend] = pipeConnection
    }

    override fun getPipeConnectionByFriend(friend: Friend): PipeConnection {
        return values[friend] ?: throw PipeConnectionForFriendNotFoundException(friend)
    }

}