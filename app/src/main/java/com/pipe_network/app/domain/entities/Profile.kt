package com.pipe_network.app.domain.entities

import com.pipe_network.app.domain.entities.pipe_messages.PipeBaseClass

data class Profile(
    val firstName: String,
    val lastName: String,
    val description: String,
    val profilePicture: ByteArray,
    val profilePictureMimeType: String,
    val publicKey: String,
) : PipeBaseClass(TYPE) {
    companion object {
        const val TYPE = "Profile"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Profile

        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (description != other.description) return false
        if (!profilePicture.contentEquals(other.profilePicture)) return false
        if (profilePictureMimeType != other.profilePictureMimeType) return false
        if (publicKey != other.publicKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + profilePicture.hashCode()
        result = 31 * result + profilePictureMimeType.hashCode()
        result = 31 * result + publicKey.hashCode()
        return result
    }
}