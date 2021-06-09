package com.pipe_network.app.android.ui.setup

import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pipe_network.app.android.MainActivity
import com.pipe_network.app.R
import com.pipe_network.app.android.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView


@AndroidEntryPoint
class SetupActivity : AppCompatActivity() {
    private val setupViewModel by viewModels<SetupViewModel>()

    private lateinit var profilePicture: CircleImageView

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val getContent = registerForActivityResult(
            ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            Log.d(TAG, "Got file by intent: ${uri.toString()}")
            if (uri != null) {
                profilePicture.setImageURI(uri)
                setupViewModel.profilePictureUri.postValue(uri)
            }
        }

        setContentView(R.layout.setup_activity)

        profilePicture = requireViewById(R.id.profile_picture)

        requireViewById<FloatingActionButton>(R.id.chooseProfilePictureButton)
            .setOnClickListener {
                getContent.launch("image/*")
            }

        findViewById<EditText>(R.id.firstNameEditText)
            .doOnTextChanged { text, _, _, _ ->
                setupViewModel.firstName.value = text.toString()
            }

        findViewById<EditText>(R.id.lastNameEditText)
            .doOnTextChanged { text, _, _, _ ->
                setupViewModel.lastName.value = text.toString()
            }

        findViewById<EditText>(R.id.descriptionEditText)
            .doOnTextChanged { text, _, _, _ ->
                setupViewModel.description.value = text.toString()
            }

        findViewById<Button>(R.id.setupActionButton).setOnClickListener {
            setupViewModel.doSetup(applicationContext)
        }

        setupViewModel.saveStatus.observe(this) {
            if (it == Status.SUCCESS) {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
    }

    companion object {
        const val TAG = "SetupFragment"
    }
}