package com.pipe_network.app.android.ui.create_feed


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pipe_network.app.R
import com.pipe_network.app.android.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateFeedActivity : AppCompatActivity() {
    private val createFeedViewModel by viewModels<CreateFeedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_feed_activity)
        findViewById<Button>(R.id.createFeedButton).setOnClickListener {
            createFeedViewModel.createFeed()
        }
        findViewById<EditText>(R.id.textEditText).doOnTextChanged { text, _, _, _ ->
            createFeedViewModel.text.value = text.toString()
        }

        createFeedViewModel.createFeedStatus.observe(this) {
            if (it == Status.SUCCESS) {
                finish()
            }
        }
    }

    companion object {
        const val TAG = "CreateFeedActivity"
    }
}