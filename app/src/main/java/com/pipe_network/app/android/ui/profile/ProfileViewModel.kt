package com.pipe_network.app.android.ui.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.pipe_network.app.android.utils.Status
import com.pipe_network.app.application.repositories.FeedRepository
import com.pipe_network.app.application.repositories.ProfileRepository
import com.pipe_network.app.application.services.ProfilePictureService
import com.pipe_network.app.infrastructure.databases.ApplicationDatabase
import com.pipe_network.app.infrastructure.models.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val profilePictureService: ProfilePictureService,
    private val feedRepository: FeedRepository,
    application: Application
) : AndroidViewModel(application) {
    val firstName by lazy {
        MutableLiveData("")
    }
    val lastName by lazy {
        MutableLiveData("")
    }
    val description by lazy {
        MutableLiveData("")
    }
    val profilePictureUri by lazy {
        MutableLiveData<Uri>(null)
    }
    val profile = profileRepository.getLive()
    val saveStatus = MutableLiveData(Status.INIT)

    val userFeeds by lazy {
        feedRepository.allLive()
    }

    fun save() {
        viewModelScope.launch {
            saveStatus.postValue(Status.LOADING)
            try {
                profile.value?.let {
                    val profileCopy = it.copy()
                    profileCopy.firstName = firstName.value!!
                    profileCopy.lastName = lastName.value!!
                    profileCopy.description = description.value!!

                    if (profileCopy.profilePicturePath != profilePictureUri.value?.path) {
                        val profilePictureFile =
                            profilePictureService.copySetupDataPictureAsProfilePicture(
                                getApplication(),
                                profilePictureUri.value!!,
                            )
                        profileCopy.profilePicturePath = profilePictureFile.absolutePath
                    }
                    profileRepository.update(profileCopy)
                }
                saveStatus.postValue(Status.SUCCESS)
            } catch (exception: Exception) {
                Log.e(TAG, exception.message.toString())
                saveStatus.postValue(Status.ERROR)
            }
        }
    }

    companion object {
        const val TAG = "ProfileViewModel"
    }
}