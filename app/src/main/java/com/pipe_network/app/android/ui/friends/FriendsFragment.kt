package com.pipe_network.app.android.ui.friends

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.pipe_network.app.R
import com.pipe_network.app.android.ui.add_friend.AddFriendActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsFragment : Fragment() {
    private val friendsViewModel by viewModels<FriendsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.friends_fragment, container, false)
        val friendsListAdapter = FriendsListAdapter()

        val friendsList = root.findViewById<RecyclerView>(R.id.friendsList)
        friendsList.adapter = friendsListAdapter
        friendsList.layoutManager = LinearLayoutManager(requireContext())

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.RED)
                val backgroundCornerOffset = 20

                when {
                    dX > 0 -> { // Swiping to the right
                        background.setBounds(
                            itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + dX.toInt() + backgroundCornerOffset,
                            itemView.getBottom()
                        )
                    }
                    else -> { // view is unSwiped
                        background.setBounds(0, 0, 0, 0)
                    }
                }
                background.draw(c)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val friend = friendsViewModel.friends.value?.get(viewHolder.adapterPosition)!!
                val text = if (friend.isInitialized()) {
                    friend.fullName()
                } else {
                    friend.publicKey
                }

                Snackbar.make(root, "Deleted $text", Snackbar.LENGTH_LONG).setAction(
                    getText(R.string.undo)
                ) {

                }.show()
            }
        }).attachToRecyclerView(friendsList)

        friendsViewModel.friends.observe(viewLifecycleOwner) {
            friendsListAdapter.setFriends(it)
        }

        root.findViewById<FloatingActionButton>(R.id.addFriend).setOnClickListener {
            startActivity(Intent(requireContext(), AddFriendActivity::class.java))
        }

        return root
    }

}