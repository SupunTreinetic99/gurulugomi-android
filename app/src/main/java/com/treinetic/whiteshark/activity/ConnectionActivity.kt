package com.treinetic.whiteshark.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.splash.SplashActivity
import com.treinetic.whiteshark.databinding.ActivityNoConnectionBinding
import com.treinetic.whiteshark.services.BookService

class ConnectionActivity : BaseActivity() {

    private lateinit var binding : ActivityNoConnectionBinding

    companion object {
        fun show(context: Context) {
            val intent = Intent(context, ConnectionActivity::class.java)
            context.startActivity(intent)
        }
        const val TOKEN_EXPIRE = 401
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoConnectionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.settingsbtn.setOnClickListener { startNetworkSettings() }
        binding.offlineBtn.setOnClickListener { startOfflineMode() }
        setupNetConnectionListener()
    }

    private fun startNetworkSettings() {
        startActivity(Intent(android.provider.Settings.ACTION_SETTINGS));
    }

    private fun startOfflineMode() {
        // TODO need to write offline mode code
        BookService.getInstance().myLibrary = null
        startActivity(Intent(this, SplashActivity::class.java))
    }


    private fun setupNetConnectionListener() {
        networkListerner = { connectivity ->
            connectivity?.let {
                if (isConnected(it)) {
                    onBackPressed()
                    finish()
                }
            }
        }
    }


    override fun onBackPressed() {
        return
    }
}
