package com.pipe_network.app.android.ui.friends

import androidx.lifecycle.ViewModel
import com.pipe_network.app.infrastructure.databases.ApplicationDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    applicationDatabase: ApplicationDatabase,
) : ViewModel() {
    val friends = applicationDatabase.friendDao().allLive()
}