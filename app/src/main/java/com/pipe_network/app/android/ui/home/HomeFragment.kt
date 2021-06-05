package com.pipe_network.app.android.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pipe_network.app.R
import com.pipe_network.app.android.ui.create_feed.CreateFeedActivity
import com.pipe_network.app.application.factories.PipeConnectionFactory
import com.pipe_network.app.application.repositories.FriendRepository
import com.pipe_network.app.domain.entities.Feed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val homeViewModel by viewModels<HomeViewModel>()

    @Inject
    lateinit var pipeConnectionFactory: PipeConnectionFactory

    @Inject
    lateinit var friendRepository: FriendRepository

    private lateinit var feedsAdapter: FeedsAdapter

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.home_fragment, container, false)
        val swipeRefreshFeed = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshFeed)
        swipeRefreshFeed.setOnRefreshListener {
            Log.d(TAG, "Reload fetch new feed")
            homeViewModel.fetchFeeds()
            swipeRefreshFeed.isRefreshing = false
        }

        feedsAdapter = FeedsAdapter()
        val feedsRecyclerView = root.findViewById<RecyclerView>(R.id.feedsRecyclerView)
        feedsRecyclerView.adapter = feedsAdapter
        feedsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        root.findViewById<FloatingActionButton>(R.id.addFeedFloatingButton).setOnClickListener {
            startActivity(Intent(requireContext(), CreateFeedActivity::class.java))
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            val friend = friendRepository.getById(1)
            if (friend != null) {
                feedsAdapter.setFeeds(
                    listOf(
                        Feed("", "Hello World!", friend, BigInteger.valueOf(0)),
                        Feed("", "Another one", friend, BigInteger.valueOf(0)),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}