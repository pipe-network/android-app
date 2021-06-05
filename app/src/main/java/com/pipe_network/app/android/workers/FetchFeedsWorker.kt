package com.pipe_network.app.android.workers

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pipe_network.app.application.factories.PipeConnectionFactory
import com.pipe_network.app.application.repositories.FriendRepository
import com.pipe_network.app.domain.models.PipeConnection
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.lang.Exception

@HiltWorker
class FetchFeedsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    lateinit var pipeConnection: PipeConnection

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FetchFeedsWorkerEntryPoint {
        fun friendRepository(): FriendRepository
        fun pipeConnectionFactory(): PipeConnectionFactory
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override suspend fun doWork(): Result {
        Log.d(TAG, "Hello from worker!")
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(
                applicationContext,
                FetchFeedsWorkerEntryPoint::class.java
            )

        val friendRepository = hiltEntryPoint.friendRepository()
        val pipeConnectionFactory = hiltEntryPoint.pipeConnectionFactory()

        val id = inputData.getInt(FRIEND_ID_DATA_KEY, -1)
        val friend = friendRepository.getById(id) ?: throw Exception("Could not find friend")
//            pipeConnection = pipeConnectionFactory.createResponderPipeConnection(
//                applicationContext,
//                object : PipeConnectionObserver {
//                    override fun onSignalingStateChanged(
//                        signalingStateChangedEvent: SignalingStateChangedEvent,
//                    ) {
//                        Log.d(TAG, "Signaling has changed: ${signalingStateChangedEvent.state}")
//                    }
//
//                    override fun onHandover() {
//                        Log.d(TAG, "Handover done!")
//                    }
//
//                    override fun onMessage(byteArray: ByteArray) {
//                        handleMessage(byteArray)
//                    }
//                },
//                friend.publicKey,
//            )
//
//            // val objectMapper = ObjectMapper(MessagePackFactory())
//
//            pipeConnection.connect()
        return Result.success()
    }

    fun handleMessage(byteArray: ByteArray) {
        Log.d(TAG, "handle message")
        pipeConnection.disconnect()
    }

    companion object {
        const val TAG = "FetchFeedsWorker"
        const val FRIEND_ID_DATA_KEY = "friend_id"

        fun uniqueWorkerName(publicKey: String): String {
            return "FetchFeedsWorkerPK{$publicKey}"
        }
    }
}