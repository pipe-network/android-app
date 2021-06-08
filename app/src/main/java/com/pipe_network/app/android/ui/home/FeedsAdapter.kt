package com.pipe_network.app.android.ui.home

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
import com.pipe_network.app.domain.entities.Feed
import java.lang.IllegalArgumentException

class FeedsAdapter :
    RecyclerView.Adapter<BaseViewHolder<Feed>>() {

    enum class FeedsViewTypes {
        TEXT_FEED;

        companion object {
            private val types = values().associateBy { it.ordinal }
            fun findByValue(value: Int) = types[value]
        }
    }

    private var feeds: MutableList<Feed> = mutableListOf()

    class TextFeedHolder(view: View) : BaseViewHolder<Feed>(view), View.OnClickListener {
        private val profilePicture: ImageView = view.findViewById(R.id.profile_picture)
        private val fullName: TextView = view.findViewById(R.id.full_name)
        private val feedText: TextView = view.findViewById(R.id.feed_text)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            Log.d("TextFeedHolder", "on click")
        }

        override fun bind(item: Feed) {
            fullName.text = "${item.friend.firstName} ${item.friend.lastName}"
            profilePicture.setImageURI(item.friend.getProfilePictureFile().toUri())
            feedText.text = item.text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Feed> {
        val context = parent.context

        return when (FeedsViewTypes.findByValue(viewType)) {
            FeedsViewTypes.TEXT_FEED -> {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.text_feed_item,
                    parent,
                    false,
                )
                TextFeedHolder(view)
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
                return feeds[oldItemPosition].uuid == newFeeds[newItemPosition].uuid
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