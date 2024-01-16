package com.treinetic.whiteshark.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.ActivityTermsAndPrivacyBinding
import com.treinetic.whiteshark.fragments.payment.AppWebViewClient


class TermsAndPrivacyActivity : BaseActivity() {

    private lateinit var binding : ActivityTermsAndPrivacyBinding

    companion object {
        fun show(context: Context, url: String) {
            val intent = Intent(context, TermsAndPrivacyActivity::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityTermsAndPrivacyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onResume() {
        super.onResume()
        setupView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupView() {
        val url: String? = intent.extras?.getString("url", null)

        url?.let {
            binding.webView.settings.javaScriptEnabled = true
            binding.webView.settings.domStorageEnabled = true
            binding.webView.webViewClient = CustomWebViewClient().apply {
                onFinished = {

                    fadeOut(view = binding.loadingLayer, onAnimationEnd = {
                        binding.loadingLayer.visibility = View.GONE
                    })
                }
            }
            binding.webView.loadUrl(it)
        }
    }
}

class CustomWebViewClient : WebViewClient() {
    var onFinished: (() -> Unit)? = null
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.d("AppWebViewClient", "onLoadResource ->")
        onFinished?.let { it() }
    }

}


