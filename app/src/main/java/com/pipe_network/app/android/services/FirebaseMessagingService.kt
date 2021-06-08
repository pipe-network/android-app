package com.pipe_network.app.android.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pipe_network.app.R
import com.pipe_network.app.android.MainActivity
import com.pipe_network.app.application.factories.PipeConnectionFactory
import com.pipe_network.app.application.handlers.IncomingPipeMessageHandler
import com.pipe_network.app.application.observers.PipeConnectionObserver
import com.pipe_network.app.application.repositories.FriendRepository
import com.pipe_network.app.application.repositories.ProfileRepository
import com.pipe_network.app.application.services.AddDeviceTokenService
import com.pipe_network.app.domain.models.PipeConnection
import com.pipe_network.app.infrastructure.models.Friend
import com.pipe_network.app.infrastructure.models.Profile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.saltyrtc.client.events.SignalingStateChangedEvent
import org.webrtc.DataChannel
import org.webrtc.PeerConnection
import javax.inject.Inject


@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pipeConnectionFactory: PipeConnectionFactory

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var friendRepository: FriendRepository

    @Inject
    lateinit var incomingPipeMessageHandler: IncomingPipeMessageHandler

    @Inject
    lateinit var addDeviceTokenService: AddDeviceTokenService

    lateinit var pipeConnection: PipeConnection

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // For more see: https://firebase.google.com/docs/cloud-messaging/concept-options

        Log.d(TAG, "Got notification")
        Log.d(TAG, "Remotemessage data: ${remoteMessage.data}")
        if (remoteMessage.data.containsKey("type") && remoteMessage.data["type"] == "wakeup") {
            val responderPublicKey = remoteMessage.data["publicKey"] ?: ""
            CoroutineScope(Dispatchers.IO).launch {
                val profile = profileRepository.get()!!
                val friend = friendRepository.getByPublicKey(responderPublicKey)

                if (friend == null) {
                    Log.d(TAG, "Unknown friend: $responderPublicKey")
                    return@launch
                }

                Log.d(
                    TAG,
                    "connecting as initiator with public key: ${profile.publicKey} to $responderPublicKey"
                )
                val initiatorPipeConnectionObserver = InitiatorPipeConnectionObserver(
                    profile,
                    friend,
                    incomingPipeMessageHandler,
                )
                pipeConnection = pipeConnectionFactory.createInitiatorPipeConnection(
                    applicationContext,
                    initiatorPipeConnectionObserver,
                    profile.privateKey,
                    responderPublicKey,
                )
                initiatorPipeConnectionObserver.pipeConnection = pipeConnection

                try {
                    pipeConnection.connect()
                } catch (exception: Exception) {
                    Log.e(TAG, exception.toString())
                }
            }

            remoteMessage.notification?.let {
                Log.d(TAG, "Message Notification Body: ${it.body}")
                sendNotification(it.body.toString())
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        addDeviceTokenService.newDeviceToken(token)
    }

    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_menu_camera)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private class InitiatorPipeConnectionObserver(
        val profile: Profile,
        val friend: Friend,
        val incomingPipeMessageHandler: IncomingPipeMessageHandler,
    ) : PipeConnectionObserver {
        lateinit var pipeConnection: PipeConnection


        override fun onSignalingStateChanged(signalingStateChangedEvent: SignalingStateChangedEvent) {
            Log.d(
                TAG,
                "signaling state changed: ${signalingStateChangedEvent.state}"
            )
        }

        override fun onPeerConnectionSignalingStateChanged(
            peerConnectionSignalingState: PeerConnection.SignalingState
        ) {
            Log.d(
                TAG,
                "peer connection signaling state changed: ${peerConnectionSignalingState.name}"
            )
        }

        override fun onHandover() {
            Log.d(TAG, "handover")
        }

        override fun onDataChannelContextMessage(byteArray: ByteArray) {
            incomingPipeMessageHandler.handle(friend, true, pipeConnection, profile, byteArray) {}
        }

        override fun onDataChannelContextStateChange(state: DataChannel.State) {
            Log.d(TAG, "data channel context state change: ${state.name}")
        }
    }

    companion object {
        private const val TAG = "MessagingService"
    }
}