package com.pipe_network.app.android.ui.setup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pipe_network.app.android.utils.Resource
import com.pipe_network.app.android.utils.Status
import com.pipe_network.app.application.services.SetupService
import com.pipe_network.app.domain.entities.SetupData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val setupService: SetupService,
) : ViewModel() {

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
    val saveStatus by lazy {
        MutableLiveData<Status>()
    }

    fun doSetup(context: Context) {
        viewModelScope.launch {
            saveStatus.postValue(Status.LOADING)
            try {
                setupService.setup(
                    context,
                    SetupData(
                        firstName.value!!,
                        lastName.value!!,
                        description.value!!,
                        profilePictureUri.value,
                    )
                )
            } catch (exception: Exception) {
                saveStatus.postValue(Status.ERROR)
            } finally {
                saveStatus.postValue(Status.SUCCESS)
            }
        }
    }
}