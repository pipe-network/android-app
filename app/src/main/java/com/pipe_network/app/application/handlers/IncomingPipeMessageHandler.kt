package com.pipe_network.app.application.handlers

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.pipe_network.app.application.repositories.FriendRepository
import com.pipe_network.app.application.repositories.FeedRepository
import com.pipe_network.app.application.services.SyncForeignFeedsService
import com.pipe_network.app.domain.entities.Feed
import com.pipe_network.app.domain.entities.FeedHead
import com.pipe_network.app.domain.entities.OutgoingFeed
import com.pipe_network.app.domain.entities.pipe_messages.*
import com.pipe_network.app.domain.models.PipeConnection
import com.pipe_network.app.infrastructure.models.Friend
import com.pipe_network.app.infrastructure.models.Profile
import dagger.hilt.android.qualifiers.ApplicationContext
import de.datlag.mimemagic.MimeData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.msgpack.jackson.dataformat.MessagePackFactory
import java.io.File
import java.util.*
import javax.inject.Inject

interface IncomingPipeMessageHandler {
    fun handle(
        friend: Friend,
        isInitiator: Boolean,
        pipeConnection: PipeConnection,
        profile: Profile,
        byteArray: ByteArray,
    )
}

class IncomingPipeMessageHandlerImpl @Inject constructor(
    @ApplicationContext val context: Context
) : IncomingPipeMessageHandler {

    @Inject
    lateinit var friendRepository: FriendRepository

    @Inject
    lateinit var feedRepository: FeedRepository

    @Inject
    lateinit var syncForeignFeedsService: SyncForeignFeedsService

    private val scope = CoroutineScope(Dispatchers.IO)
    private val objectMapper = ObjectMapper(MessagePackFactory()).registerKotlinModule()

    private var feedsToReceive = -1
    private var feedsReceived = 0

    @SuppressLint("LongLogTag")
    override fun handle(
        friend: Friend,
        isInitiator: Boolean,
        pipeConnection: PipeConnection,
        profile: Profile,
        byteArray: ByteArray,
    ) {
        try {
            val requestTypeMessage =
                objectMapper.readValue(byteArray, RequestTypeMessage::class.java)

            if (requestTypeMessage.type == RequestTypeMessage.REQUEST_PROFILE) {
                onRequestProfile(isInitiator, pipeConnection, profile)
            }

            if (requestTypeMessage.type == RequestTypeMessage.REQUEST_FEED_HEADS) {
                onRequestFeedHeads(pipeConnection)
            }
            return
        } catch (exception: Exception) {
            handleException(exception)
        }

        try {
            val respondProfile = objectMapper.readValue(byteArray, RespondProfile::class.java)
            onRespondProfile(respondProfile)
            if (!isInitiator) {
                pipeConnection.sendMessage(
                    objectMapper.writeValueAsBytes(
                        RequestTypeMessage.createAsRequestFeedHeads()
                    )
                )
            }
            return
        } catch (exception: Exception) {
            handleException(exception)
        }

        try {
            val respondFeedHeads = objectMapper.readValue(byteArray, RespondFeedHeads::class.java)
            onRespondFeedHeads(pipeConnection, respondFeedHeads)
            return
        } catch (exception: Exception) {
            handleException(exception)
        }

        try {
            val requestFeed = objectMapper.readValue(byteArray, RequestFeed::class.java)
            onRequestFeed(pipeConnection, requestFeed)
            return
        } catch (exception: Exception) {
            handleException(exception)
        }

        try {
            val respondFeed = objectMapper.readValue(byteArray, RespondFeed::class.java)
            onRespondFeed(friend, pipeConnection, respondFeed)
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
            Log.d(TAG, "Outgoing: RequestProfile")
            pipeConnection.sendMessage(
                objectMapper.writeValueAsBytes(
                    RequestTypeMessage.createAsRequestProfile()
                )
            )
        }

        scope.launch {
            val respondProfile = RespondProfile(profile.getProfileEntity())
            val respondProfileBytes = objectMapper.writeValueAsBytes(respondProfile)
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
    fun onRequestFeedHeads(pipeConnection: PipeConnection) {
        Log.d(TAG, "Incoming: RequestFeedHeads")
        scope.launch {
            val feeds = feedRepository.all()
            val feedHeads = feeds.map {
                FeedHead(it.id.toString())
            }
            val respondFeedHeadsBytes = objectMapper.writeValueAsBytes(RespondFeedHeads(feedHeads))
            Log.d(TAG, "Outgoing: RespondFeedHeads")
            pipeConnection.sendMessage(respondFeedHeadsBytes)
        }
    }

    @SuppressLint("LongLogTag")
    fun onRequestFeed(pipeConnection: PipeConnection, requestFeed: RequestFeed) {
        Log.d(TAG, "Incoming: RequestFeed")
        scope.launch {
            val feed = feedRepository.get(UUID.fromString(requestFeed.uuid))
            feed?.let {
                val outgoingFeed = OutgoingFeed(
                    it.id.toString(),
                    it.text,
                    it.created.time,
                )
                val respondFeedBytes = objectMapper.writeValueAsBytes(RespondFeed(outgoingFeed))
                Log.d(TAG, "Outgoing: RespondFeed")
                pipeConnection.sendMessage(respondFeedBytes)
            }
        }
    }

    @SuppressLint("LongLogTag")
    fun onRespondFeedHeads(pipeConnection: PipeConnection, respondFeedHeads: RespondFeedHeads) {
        Log.d(TAG, "Incoming: RespondFeedHeads")
        scope.launch {
            Log.d(TAG, "RespondFeedHeads size: ${respondFeedHeads.feedHeads.size}")
            val feedHeadsToRetrieve = syncForeignFeedsService.getToRetrieveFeedHeads(
                respondFeedHeads.feedHeads
            )

            feedsToReceive = feedHeadsToRetrieve.size

            for (feedHead in feedHeadsToRetrieve) {
                val requestFeedBytes = objectMapper.writeValueAsBytes(RequestFeed(feedHead.uuid))
                Log.d(TAG, "Outgoing: RequestFeed")
                pipeConnection.sendMessage(requestFeedBytes)
            }
        }
    }


    @SuppressLint("LongLogTag")
    fun onRespondFeed(
        friend: Friend,
        pipeConnection: PipeConnection,
        respondFeed: RespondFeed,
    ) {
        Log.d(TAG, "Incoming: RespondFeed")
        feedsReceived += 1

        if (feedsReceived == -1 || feedsToReceive - feedsReceived == 0) {
            pipeConnection.disconnect()
        }

        val feed = Feed(
            respondFeed.feed.uuid,
            respondFeed.feed.text,
            friend,
            respondFeed.feed.timestamp,
            respondFeed.feed.picture,
        )

        runBlocking {
            syncForeignFeedsService.storeForeignFeed(feed)
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
                    "Exception read value as: ${exception.message}"
                )
                throw exception
            }
        }
    }

    companion object {
        const val TAG = "IncomingPipeMessageHandler"
    }
}