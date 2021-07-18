package com.pipe_network.app.android.ui.friends

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pipe_network.app.R
import com.pipe_network.app.android.ui.add_friend.AddFriendActivity
import com.pipe_network.app.application.repositories.FriendRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FriendsFragment : Fragment() {
    private val friendsViewModel by viewModels<FriendsViewModel>()

    @Inject
    lateinit var friendRepository: FriendRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.friends_fragment, container, false)
        val friendsListAdapter = FriendsListAdapter(friendRepository)

        val friendsList = root.findViewById<RecyclerView>(R.id.friendsList)
        friendsList.adapter = friendsListAdapter
        friendsList.layoutManager = LinearLayoutManager(requireContext())

        friendsViewModel.friends.observe(viewLifecycleOwner) {
            friendsListAdapter.setFriends(it)
        }

        root.findViewById<FloatingActionButton>(R.id.addFriend).setOnClickListener {
            startActivity(Intent(requireContext(), AddFriendActivity::class.java))
        }

        return root
    }

}