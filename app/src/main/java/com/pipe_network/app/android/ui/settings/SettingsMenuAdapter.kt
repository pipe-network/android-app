package com.pipe_network.app.android.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pipe_network.app.R
import com.pipe_network.app.domain.entities.SettingsMenuItem


class SettingsMenuAdapter(
    private val dataSet: Array<SettingsMenuItem>,
    private val onClick: (SettingsMenuItem) -> Unit
) :
    RecyclerView.Adapter<SettingsMenuAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(settingsMenuItem: SettingsMenuItem, clickListener: (SettingsMenuItem) -> Unit) {
            view.findViewById<TextView>(R.id.settingsMenuText).text = settingsMenuItem.label
            view.setOnClickListener { clickListener(settingsMenuItem) }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.settings_item,
            viewGroup,
            false,
        )

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(dataSet[position], onClick)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}
