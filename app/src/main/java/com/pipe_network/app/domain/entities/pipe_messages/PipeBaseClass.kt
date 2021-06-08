package com.pipe_network.app.domain.entities.pipe_messages

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.pipe_network.app.domain.entities.Profile

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
@JsonSubTypes(
    Type(value = RequestTypeMessage::class, name = "RequestTypeMessage"),
    Type(value = RespondProfile::class, name = "RespondProfile"),
    Type(value = RespondFeedHeads::class, name = "RespondFeedHeads"),
    Type(value = RequestFeed::class, name = "RequestFeed"),
    Type(value = RespondFeed::class, name = "RespondFeed"),
    Type(value = Profile::class, name = "Profile"),
)
abstract class PipeBaseClass(val type: String)