package com.pipe_network.app.android.ui.profile

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pipe_network.app.R
import com.pipe_network.app.android.utils.BaseViewHolder
import com.pipe_network.app.application.repositories.FeedRepository
import com.pipe_network.app.infrastructure.models.Feed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class UserFeedsAdapter(val feedRepository: FeedRepository) :
    RecyclerView.Adapter<BaseViewHolder<Feed>>() {

    enum class FeedsViewTypes {
        TEXT_FEED;

        companion object {
            private val types = values().associateBy { it.ordinal }
            fun findByValue(value: Int) = types[value]
        }
    }

    private var feeds: MutableList<Feed> = mutableListOf()

    class TextFeedHolder(view: View, val feedRepository: FeedRepository) :
        BaseViewHolder<Feed>(view), View.OnClickListener {
        private val feedText: TextView = view.findViewById(R.id.feed_text)
        private val deleteButton: Button = view.findViewById(R.id.deleteButton)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            Log.d("TextFeedHolder", "on click")
        }

        override fun bind(item: Feed) {
            feedText.text = item.text

            deleteButton.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    feedRepository.delete(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Feed> {
        val context = parent.context

        return when (FeedsViewTypes.findByValue(viewType)) {
            FeedsViewTypes.TEXT_FEED -> {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.user_feed_item,
                    parent,
                    false,
                )
                TextFeedHolder(view, feedRepository)
            }
            else ->
                throw IllegalArgumentException("FeedViewType was not found")
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (feeds[position].hasPicture()) {
            FeedsViewTypes.TEXT_FEED.ordinal
        } else {

            // TODO add ImageFeed here
            return 0
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Feed>, position: Int) {
        holder.bind(feeds[position])
    }

    override fun getItemCount(): Int {
        return feeds.size
    }

    fun setFeeds(newFeeds: List<Feed>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return feeds.size
            }

            override fun getNewListSize(): Int {
                return newFeeds.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return feeds[oldItemPosition].id == newFeeds[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return feeds[oldItemPosition] == newFeeds[newItemPosition]
            }

        })

        result.dispatchUpdatesTo(this);
        feeds.clear()
        feeds.addAll(newFeeds)
    }
}