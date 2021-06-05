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
    Type(value = RespondProfile::class, name = "RespondProfile"),
    Type(value = RequestProfile::class, name = "RequestProfile"),
    Type(value = Profile::class, name = "Profile"),
)
abstract class PipeBaseClass(val type: String)