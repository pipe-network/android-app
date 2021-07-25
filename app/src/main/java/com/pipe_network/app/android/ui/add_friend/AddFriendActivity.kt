package com.pipe_network.app.android.ui.add_friend

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.pipe_network.app.R
import com.pipe_network.app.android.MainActivity
import com.pipe_network.app.domain.entities.AddFriendStatus
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddFriendActivity : AppCompatActivity() {
    private val addFriendViewModel by viewModels<AddFriendViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_friend_activity)

        findViewById<EditText>(R.id.publicKey)
            .doOnTextChanged { text, _, _, _ ->
                addFriendViewModel.publicKey.value = text.toString()
            }

        findViewById<Button>(R.id.addFriendAction).setOnClickListener {
            Log.d(TAG, "Adding a friend")
            addFriendViewModel.addFriend()
        }

        addFriendViewModel.addFriendStatus.observe(this) {
            Log.d(TAG, "AddFriendStatus changed: $it")
            if (it.equals(AddFriendStatus.SUCCESS)) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("navigation", R.id.navigation_friends)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
    }

    companion object {
        const val TAG = "AddFriendActivity"
    }
}