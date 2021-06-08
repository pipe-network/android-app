package com.pipe_network.app.infrastructure.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class ForeignFeed (
    @PrimaryKey
    val id: UUID,
)