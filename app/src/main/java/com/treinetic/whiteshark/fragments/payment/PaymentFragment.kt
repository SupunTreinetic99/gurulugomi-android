package com.treinetic.whiteshark.fragments.payment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Keep
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.databinding.BottomSheetRedeemloyaltyBinding
import com.treinetic.whiteshark.databinding.PaymentFragmentBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.services.PaymentService

/**
 * Created by Nuwan on 2/15/19.
 */
@Keep
class PaymentFragment : BaseFragment() {

    private val logTag = "PaymentFragment"
    private var _binding : PaymentFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View

    companion object {
        fun newInstance(): PaymentFragment {
            return PaymentFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PaymentFragmentBinding.inflate(inflater, container, false)
        mainView = binding.root
        setUpWebView()
//        TrPayNow().fragmentManager = requireFragmentManager()
        setHasOptionsMenu(true)
        hideToolbarBackButton()
//        setupToolBar()
        showToolbarBackButton()
        return mainView
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        binding.webView.settings.javaScriptEnabled = true
        var trPayNow = TrPayNow()
        trPayNow.fragmentManager = requireFragmentManager()
//        trPayNow.fragment = this
        binding.webView.addJavascriptInterface(trPayNow, "TrPayNow")
        binding.webView.webViewClient = AppWebViewClient().apply {
            onFinished = {
                fadeOut(view = binding.loadingLayer, onAnimationEnd = {
                    binding.loadingLayer.visibility = View.GONE
                })
            }
        }
        PaymentService.getInstance().paymentData?.let {
            Log.d(logTag, "url : " + it.url)
            binding.webView.loadUrl(it.url)
        }

    }


//    class TrPayNow(context: Context?) {
//
//        private val TAG = "WebAppInterFace"
//
//        @JavascriptInterface
//        fun uiLoaded() {
//            Log.e(TAG, "uiLoaded called")
//        }
//
//        @JavascriptInterface
//        fun paymentFailed() {
//            Log.e(TAG, "paymentFailed called")
//        }
//
//        @JavascriptInterface
//        fun paymentSuccess() {
//            Log.e(TAG, "paymentSuccess called")
//        }
//
//    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        setOptionMenuVisibility(
            menu, false, false, false,
            false, false
        )
    }

    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = resources.getString(R.string.order_confirmation)
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            binding.webView.removeJavascriptInterface("TrPayNow")
        }catch (e:Exception){
            e.printStackTrace()
        }

    }
}


class AppWebViewClient : WebViewClient() {

    var onFinished: (() -> Unit)? = null
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onFinished?.let { it() }
    }
}