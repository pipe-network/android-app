package com.pipe_network.app.application.handlers

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.pipe_network.app.android.ui.home.HomeViewModel
import com.pipe_network.app.application.repositories.FriendRepository
import com.pipe_network.app.application.repositories.FeedRepository
import com.pipe_network.app.domain.entities.pipe_messages.RequestProfile
import com.pipe_network.app.domain.entities.pipe_messages.RespondProfile
import com.pipe_network.app.domain.models.PipeConnection
import com.pipe_network.app.infrastructure.models.Profile
import dagger.hilt.android.qualifiers.ApplicationContext
import de.datlag.mimemagic.MimeData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.msgpack.jackson.dataformat.MessagePackFactory
import java.io.File
import javax.inject.Inject

interface IncomingPipeMessageHandler {
    fun handle(
        isInitiator: Boolean,
        pipeConnection: PipeConnection,
        profile: Profile,
        byteArray: ByteArray,
    )
}

class IncomingPipeMessageHandlerImpl @Inject constructor(
    @ApplicationContext val context: Context
) :
    IncomingPipeMessageHandler {

    @Inject
    lateinit var friendRepository: FriendRepository

    @Inject
    lateinit var feedRepository: FeedRepository

    private val scope = CoroutineScope(Dispatchers.IO)
    private val objectMapper = ObjectMapper(MessagePackFactory()).registerKotlinModule()

    @SuppressLint("LongLogTag")
    override fun handle(
        isInitiator: Boolean,
        pipeConnection: PipeConnection,
        profile: Profile,
        byteArray: ByteArray,
    ) {
        try {
            objectMapper.readValue(byteArray, RequestProfile::class.java)
            onRequestProfile(isInitiator, pipeConnection, profile)
            return
        } catch (exception: Exception) {
            handleException(exception)
        }

        try {
            val respondProfile = objectMapper.readValue(
                byteArray,
                RespondProfile::class.java,
            )

            onRespondProfile(respondProfile)
            if (isInitiator) {
                Log.d(HomeViewModel.TAG, "Closing connection")
                pipeConnection.disconnect()
            }

            return
        } catch (exception: Exception) {
            handleException(exception)
        }
    }

    @SuppressLint("LongLogTag")
    fun onRequestProfile(
        isInitiator: Boolean,
        pipeConnection: PipeConnection,
        profile: Profile,
    ) {
        Log.d(TAG, "Incoming: RequestProfile")

        if (isInitiator) {
            Log.d(HomeViewModel.TAG, "Outgoing: RequestProfile")
            pipeConnection.sendMessage(objectMapper.writeValueAsBytes(RequestProfile()))
        }

        scope.launch {
            val respondProfile = RespondProfile(profile.getProfileEntity())
            val respondProfileBytes = objectMapper.writeValueAsBytes(
                respondProfile,
            )
            Log.d(TAG, "Outgoing: RespondProfile")
            pipeConnection.sendMessage(respondProfileBytes)
        }
    }

    @SuppressLint("LongLogTag")
    fun onRespondProfile(respondProfile: RespondProfile) {
        Log.d(TAG, "Incoming: RespondProfile")
        scope.launch {
            val friend = friendRepository.getByPublicKey(respondProfile.profile.publicKey)!!
            friend.firstName = respondProfile.profile.firstName
            friend.lastName = respondProfile.profile.lastName
            friend.description = respondProfile.profile.description

            val mimeData = MimeData.fromByteArray(respondProfile.profile.profilePicture)
            val friendsProfilePictureFile = File(
                context.filesDir,
                "${friend.publicKey}.${mimeData.suffix}",
            )
            friendsProfilePictureFile.writeBytes(respondProfile.profile.profilePicture)
            friend.profilePicturePath = friendsProfilePictureFile.absolutePath
            friendRepository.update(friend)
        }
    }

    @SuppressLint("LongLogTag")
    fun handleException(exception: java.lang.Exception) {
        when (exception) {
            is MismatchedInputException -> {
                Log.w(TAG, "MismatchedInput: ${exception.message}")
            }
            else -> {
                Log.e(
                    TAG,
                    "Exception read value as RequestProfile: ${exception.message}"
                )
                throw exception
            }
        }
    }

    companion object {
        const val TAG = "IncomingPipeMessageHandler"
    }
}