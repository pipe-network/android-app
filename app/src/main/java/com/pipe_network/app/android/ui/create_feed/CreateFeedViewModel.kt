package com.pipe_network.app.android.ui.create_feed

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pipe_network.app.android.utils.Status
import com.pipe_network.app.application.repositories.FeedRepository
import com.pipe_network.app.infrastructure.models.Feed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreateFeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
) : ViewModel() {

    val text: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val createFeedStatus: MutableLiveData<Status> by lazy {
        MutableLiveData(Status.INIT)
    }

    fun createFeed() {
        viewModelScope.launch {
            createFeedStatus.postValue(Status.LOADING)
            try {
                feedRepository.create(Feed(Date(), text.value!!))
                createFeedStatus.postValue(Status.SUCCESS)
            } catch (exception: Exception) {
                createFeedStatus.postValue(Status.ERROR)
                exception.message?.let { Log.e(TAG, it) }
            }
        }
    }

    companion object {
        const val TAG = "CreateFeedVM"
    }
}