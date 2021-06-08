package com.pipe_network.app.android.ui.home

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.pipe_network.app.application.factories.PipeConnectionFactory
import com.pipe_network.app.application.handlers.IncomingPipeMessageHandler
import com.pipe_network.app.application.observers.PipeConnectionObserver
import com.pipe_network.app.application.repositories.FriendRepository
import com.pipe_network.app.application.repositories.ProfileRepository
import com.pipe_network.app.domain.entities.Feed
import com.pipe_network.app.domain.entities.pipe_messages.RequestTypeMessage
import com.pipe_network.app.domain.models.PipeConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.msgpack.jackson.dataformat.MessagePackFactory
import org.saltyrtc.client.events.SignalingStateChangedEvent
import org.webrtc.DataChannel
import org.webrtc.PeerConnection
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val profileRepository: ProfileRepository,
    private val pipeConnectionFactory: PipeConnectionFactory,
    private val incomingPipeMessageHandler: IncomingPipeMessageHandler,
    application: Application,
) : AndroidViewModel(application) {
    lateinit var pipeConnection: PipeConnection
    val feeds: MutableLiveData<MutableList<Feed>> = MutableLiveData(mutableListOf())

    @RequiresApi(Build.VERSION_CODES.N)
    fun fetchFeeds() {
        viewModelScope.launch {
            val profile = profileRepository.get()!!
            val friends = friendRepository.all()

            for (friend in friends) {
                Log.d(
                    TAG,
                    "connection as responder with public key: ${profile.publicKey} to ${friend.publicKey}"
                )
                // TODO do this asynchronous
                pipeConnection = pipeConnectionFactory.createResponderPipeConnection(
                    getApplication(),
                    object : PipeConnectionObserver {
                        override fun onSignalingStateChanged(
                            signalingStateChangedEvent: SignalingStateChangedEvent,
                        ) {
                            Log.d(TAG, "Signaling has changed: ${signalingStateChangedEvent.state}")
                        }

                        override fun onPeerConnectionSignalingStateChanged(
                            peerConnectionSignalingState: PeerConnection.SignalingState
                        ) {
                            Log.d(
                                TAG,
                                "Peer Connection signaling state has changed: ${peerConnectionSignalingState.name}"
                            )
                        }

                        override fun onHandover() {
                            Log.d(TAG, "Handover done")
                        }

                        override fun onDataChannelContextMessage(byteArray: ByteArray) {
                            incomingPipeMessageHandler.handle(
                                friend,
                                false,
                                pipeConnection,
                                profile,
                                byteArray,
                            ) {
                                Log.d(TAG, "Received a new Feed: $it")
                                Log.d(TAG, "FEEDS VALUE: ${feeds.value?.size}")
                                val feedsCopy = feeds.value ?: arrayListOf()
                                feedsCopy.add(it)
                                feeds.postValue(feedsCopy)
                            }
                        }

                        override fun onDataChannelContextStateChange(state: DataChannel.State) {
                            Log.d(TAG, "DataChannelContext state changed: $state")
                            if (state == DataChannel.State.OPEN) {
                                val objectMapper = ObjectMapper(
                                    MessagePackFactory()
                                ).registerKotlinModule()

                                Log.d(TAG, "Outgoing from HomeViewModel: RequestProfile")
                                pipeConnection.sendMessage(
                                    objectMapper.writeValueAsBytes(
                                        RequestTypeMessage.createAsRequestProfile()
                                    ),
                                )
                            }
                        }
                    },
                    profile.privateKey,
                    friend.publicKey,
                )

                pipeConnection.connect()
            }
        }
    }

    companion object {
        const val TAG = "HomeViewModel"
    }
}