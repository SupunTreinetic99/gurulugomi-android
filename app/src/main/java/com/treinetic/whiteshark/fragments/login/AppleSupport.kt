package com.treinetic.whiteshark.fragments.login

import android.graphics.Rect
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity


@Keep
class AppleWebViewInterface {

    var onTokenReceived: ((String) -> Unit)? = null
    var onSignFailed: (() -> Unit)? = null

    companion object {
        private var instance = AppleWebViewInterface()
        fun getInstance(): AppleWebViewInterface {
            return instance
        }
    }

    @JavascriptInterface
    fun LoginSuccess(token: String?) {
        onTokenReceived?.let { it(token!!) }
    }

    @JavascriptInterface
    fun LoginFailed() {
        onSignFailed?.let { it() }
    }

}

class AppleWebViewClient : WebViewClient() {
    private var clearHistory = false
    var onFinished: ((url: String) -> Unit)? = null

    companion object {

        lateinit var activity: AppCompatActivity
    }

    fun clearHistory() {
        clearHistory = true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        if (clearHistory) {
            clearHistory = false
            this.clearHistory()
        }
        super.onPageFinished(view, url)
        val displayRectangle = Rect()

        val window = activity.window
        window.decorView.getWindowVisibleDisplayFrame(displayRectangle)

        val layoutparms = view?.layoutParams
        layoutparms?.height = (displayRectangle.height() * 0.9f).toInt()
        view?.layoutParams = layoutparms

        onFinished?.let { it(url.toString()) }
    }

}