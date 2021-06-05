package com.pipe_network.app.android.ui.add_friend

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pipe_network.app.application.repositories.FriendRepository
import com.pipe_network.app.domain.entities.AddFriendStatus
import com.pipe_network.app.infrastructure.models.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
) : ViewModel() {
    val publicKey by lazy {
        MutableLiveData("")
    }

    val addFriendStatus by lazy {
        MutableLiveData(AddFriendStatus.NONE)
    }

    fun addFriend() {
        viewModelScope.launch {
            addFriendStatus.value = AddFriendStatus.LOADING
            try {
                if (publicKey.value != null) {
                    friendRepository.add(
                        Friend(
                            0,
                            "",
                            "",
                            "",
                            "",
                            publicKey.value ?: "",
                        )
                    )
                } else {
                    throw Exception("publicKey is not set")
                }
                addFriendStatus.value = AddFriendStatus.SUCCESS
            } catch (exception: Exception) {
                Log.e(TAG, exception.message.toString())
                addFriendStatus.value = AddFriendStatus.FAILED
            }
        }
    }

    companion object {
        const val TAG = "AddFriendVM"
    }
}