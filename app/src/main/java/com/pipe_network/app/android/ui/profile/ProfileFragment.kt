package com.pipe_network.app.android.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pipe_network.app.R
import com.pipe_network.app.android.ui.setup.SetupActivity
import com.pipe_network.app.android.utils.Status
import com.pipe_network.app.application.repositories.FeedRepository
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import javax.inject.Inject


@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val profileViewModel by viewModels<ProfileViewModel>()
    private lateinit var profilePicture: CircleImageView
    private lateinit var userFeedsAdapter: UserFeedsAdapter

    @Inject
    lateinit var feedRepository: FeedRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.profile_fragment, container, false)
        val getContent = registerForActivityResult(
            ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            Log.d(SetupActivity.TAG, "Got file by intent: ${uri.toString()}")
            if (uri != null) {
                profileViewModel.profilePictureUri.postValue(uri)
            }
        }

        profilePicture = root.findViewById(R.id.profile_picture)
        val firstNameEditText = root.findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = root.findViewById<EditText>(R.id.lastNameEditText)
        val descriptionEditText = root.findViewById<EditText>(R.id.descriptionEditText)
        val publicKeyEditText = root.findViewById<EditText>(R.id.profilePublicKey)
        val copyPublicKeyFloatingActionButton = root.findViewById<FloatingActionButton>(
            R.id.copyPublicKeyFloatingButton
        )
        val saveButton = root.findViewById<Button>(R.id.saveButton)

        root.findViewById<FloatingActionButton>(R.id.chooseProfilePictureButton)
            .setOnClickListener {
                getContent.launch("image/*")
            }


        firstNameEditText.doOnTextChanged { text, _, _, _ ->
            profileViewModel.firstName.value = text.toString()
            saveButton.text = getString(R.string.save)
        }


        lastNameEditText.doOnTextChanged { text, _, _, _ ->
            profileViewModel.lastName.value = text.toString()
            saveButton.text = getString(R.string.save)
        }


        descriptionEditText.doOnTextChanged { text, _, _, _ ->
            profileViewModel.description.value = text.toString()
            saveButton.text = getString(R.string.save)
        }

        saveButton.setOnClickListener {
            profileViewModel.save()
        }

        profileViewModel.profilePictureUri.observe(viewLifecycleOwner) {
            profilePicture.setImageURI(it)
            saveButton.text = getString(R.string.save)
        }

        copyPublicKeyFloatingActionButton.setOnClickListener {
            val clipboardManager: ClipboardManager? = getSystemService(
                requireContext(),
                ClipboardManager::class.java,
            )
            val clipData = ClipData.newPlainText(
                "Pipe Key",
                profileViewModel.profile.value?.publicKey,
            )
            clipboardManager?.setPrimaryClip(clipData)
            val duration = Toast.LENGTH_SHORT

            val toast =
                Toast.makeText(requireContext(), getString(R.string.pipe_key_copied), duration)
            toast.show()

        }

        profileViewModel.saveStatus.observe(viewLifecycleOwner) {
            saveButton.isEnabled = true
            when (it) {
                Status.LOADING ->
                    saveButton.isEnabled = false
                Status.SUCCESS ->
                    saveButton.text = getString(R.string.saved)
                else -> {
                }
            }
        }

        profileViewModel.profile.observe(viewLifecycleOwner) {
            firstNameEditText.setText(it.firstName)
            lastNameEditText.setText(it.lastName)
            descriptionEditText.setText(it.description)
            publicKeyEditText.setText(it.publicKey)

            profileViewModel.profilePictureUri.value = it.getProfilePictureUri()
        }

        userFeedsAdapter = UserFeedsAdapter(feedRepository)
        val feedsRecyclerView = root.findViewById<RecyclerView>(R.id.userFeedsRecyclerView)
        feedsRecyclerView.adapter = userFeedsAdapter
        feedsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        profileViewModel.userFeeds.observe(viewLifecycleOwner) { list ->
            userFeedsAdapter.setFeeds(list)
        }

        return root
    }
}