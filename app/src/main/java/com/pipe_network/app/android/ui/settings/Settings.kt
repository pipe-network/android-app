package com.pipe_network.app.android.ui.settings

import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pipe_network.app.R
import com.pipe_network.app.android.ui.setup.SetupActivity
import com.pipe_network.app.domain.entities.DELETE_ALL_DATA
import com.pipe_network.app.domain.entities.SettingsMenuItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Settings : Fragment() {
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val settingsViewModel by viewModels<SettingsViewModel>()

        val root = inflater.inflate(R.layout.settings_fragment, container, false)
        val settingsMenuAdapter = SettingsMenuAdapter(
            arrayOf(
                SettingsMenuItem(
                    DELETE_ALL_DATA,
                    getString(R.string.settings_menu_delete_all_data)
                ),
            ),
        ) {
            if (it.id == DELETE_ALL_DATA) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.settings_menu_delete_all_data)
                builder.setMessage(getString(R.string.delete_warning))
                builder.setPositiveButton(R.string.yes) { _, _ ->
                    settingsViewModel.purge()
                    val intent = Intent(requireContext(), SetupActivity::class.java)
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }

                builder.setNegativeButton(android.R.string.cancel) { _, _ -> }
                builder.show()
            }
            Log.d("Settings: ", it.toString())
        }
        val settingsMenuView = root.findViewById<RecyclerView>(R.id.settingsMenuView)
        settingsMenuView.adapter = settingsMenuAdapter
        settingsMenuView.layoutManager = LinearLayoutManager(requireContext())
        settingsMenuView.itemAnimator = DefaultItemAnimator()
        settingsMenuView.addItemDecoration(DividerItemDecoration(this.requireContext(), 1))
        return root
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}