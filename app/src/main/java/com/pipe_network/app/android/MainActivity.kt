package com.pipe_network.app.android

import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.pipe_network.app.R
import com.pipe_network.app.android.ui.setup.SetupActivity
import com.pipe_network.app.application.services.AddDeviceTokenService
import com.pipe_network.app.application.services.SetupService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var setupService: SetupService

    @Inject
    lateinit var addDeviceTokenService: AddDeviceTokenService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSetupFragmentResult()
        initFCM()
        val context = applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            if (setupService.isSetupNeeded()) {
                runOnUiThread {
                    val intent = Intent(context, SetupActivity::class.java)
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
            } else {
                runOnUiThread {
                    initMainApp()
                }
            }
        }
    }

    private fun initSetupFragmentResult() {
        supportFragmentManager.setFragmentResultListener(
            "setupDone",
            this,
        ) { _, bundle ->
            val setupDone = bundle.getBoolean("setupDone")
            if (setupDone) {
                initMainApp()
            }
        }
    }

    private fun initSetup() {
        setContentView(R.layout.activity_main_setup)
    }

    private fun initMainApp() {
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment,
        ) as NavHostFragment
        val navController = navHostFragment.navController

        navView.setupWithNavController(navController)
    }

    private fun initFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            addDeviceTokenService.newDeviceToken(token!!)
            // Log and toast
            Log.d(TAG, getString(R.string.msg_token_fmt, token))
        })
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}