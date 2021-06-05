package com.pipe_network.app.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pipe_network.app.application.services.PurgeService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val purgeService: PurgeService,
) : ViewModel() {
    fun purge() {
        viewModelScope.launch {
            purgeService.purge()
        }
    }
}