package com.pipe_network.app.android.ui.friends

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pipe_network.app.R
import com.pipe_network.app.android.utils.BaseViewHolder
import com.pipe_network.app.application.repositories.FriendRepository
import com.pipe_network.app.infrastructure.models.Friend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class FriendsListAdapter(val friendRepository: FriendRepository) :
    RecyclerView.Adapter<BaseViewHolder<Friend>>() {

    enum class FriendsViewTypes {
        INITIATED_FRIEND,
        UNINITIATED_FRIEND;

        companion object {
            private val types = values().associateBy { it.ordinal }
            fun findByValue(value: Int) = types[value]
        }
    }

    private var friends: List<Friend> = listOf()

    class UninitiatedFriendHolder(view: View, val friendRepository: FriendRepository) : BaseViewHolder<Friend>(view) {
        private val publicKey: TextView = view.findViewById(R.id.uninitiatedFriendPublicKey)
        private val deleteUninitiatedFriendButton: TextView = view.findViewById(R.id.deleteUninitiatedFriend)

        override fun bind(item: Friend) {
            publicKey.text = item.publicKey

            deleteUninitiatedFriendButton.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    friendRepository.delete(item)
                }
            }
        }
    }

    class InitiatedFriendHolder(view: View, val friendRepository: FriendRepository) : BaseViewHolder<Friend>(view), View.OnClickListener {
        private val friendName: TextView = view.findViewById(R.id.initiatedFriendName)
        private val friendProfilePicture: ImageView = view.findViewById(
            R.id.initiatedFriendProfilePicture,
        )
        private val friendDescription: TextView = view.findViewById(
            R.id.inititatedFriendDescription,
        )
        private val deleteInitiatedFriendButton: TextView = view.findViewById(R.id.deleteUninitiatedFriend)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            Log.d("FriendViewHolder", "on click")
        }

        override fun bind(item: Friend) {
            friendName.text = "${item.firstName} ${item.lastName}"
            friendProfilePicture.setImageURI(item.getProfilePictureFile().toUri())
            friendDescription.text = item.description

            deleteInitiatedFriendButton.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    friendRepository.delete(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Friend> {
        val context = parent.context

        return when (FriendsViewTypes.findByValue(viewType)) {
            FriendsViewTypes.UNINITIATED_FRIEND -> {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.uninitiated_friend_item,
                    parent,
                    false,
                )
                UninitiatedFriendHolder(view, friendRepository)
            }
            FriendsViewTypes.INITIATED_FRIEND -> {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.inititated_friend_item,
                    parent,
                    false,
                )
                InitiatedFriendHolder(view, friendRepository)
            }
            else ->
                throw IllegalArgumentException("FriendViewType was not found")
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (friends[position].firstName != "" || friends[position].lastName != "") {
            FriendsViewTypes.INITIATED_FRIEND.ordinal
        } else {
            FriendsViewTypes.UNINITIATED_FRIEND.ordinal
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Friend>, position: Int) {
        holder.bind(friends[position])
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    fun setFriends(newFriends: List<Friend>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return friends.size
            }

            override fun getNewListSize(): Int {
                return newFriends.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return friends[oldItemPosition].id == newFriends[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return friends[oldItemPosition] == newFriends[newItemPosition]
            }

        })

        friends = newFriends
        result.dispatchUpdatesTo(this);
    }
}