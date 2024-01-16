package com.treinetic.whiteshark.fragments.login


import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.jaredrummler.android.device.DeviceName
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.LoginActivity
import com.treinetic.whiteshark.activity.TermsAndPrivacyActivity
import com.treinetic.whiteshark.constance.Contants
import com.treinetic.whiteshark.constance.DeviceInformation
import com.treinetic.whiteshark.databinding.FragmentLoginBinding
import com.treinetic.whiteshark.databinding.FragmentMyLibraryBinding
import com.treinetic.whiteshark.dialog.BottomSheetBaseDialog
import com.treinetic.whiteshark.dialog.MaterialDialogs
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.appupdate.Update
import com.treinetic.whiteshark.services.*
import com.treinetic.whiteshark.util.FiledValidate
import com.treinetic.whiteshark.util.Keyboard
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.security.MessageDigest


class LoginFragment : BaseFragment(), View.OnClickListener {


    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private var materialDialogs: MaterialDialogs = MaterialDialogs()
    private lateinit var model: LoginViewModel
    private val GOOGLE_LOGIN_REQUEST_CODE = 1001
    private lateinit var filedList: MutableList<EditText>
    private val NO_EMPTY = 0
    private lateinit var BUTTON_BACKROUND: Drawable
    private lateinit var FB_BACKROUND: Drawable
    private lateinit var GOOGLE_BACKROUND: Drawable
    private lateinit var APPLE_BACKROUND: Drawable
    private val INVALID_CREDENITAIL = 400
    private val USER_NOT_ACTIVATED = 401
    var animator: ValueAnimator? = null
    var googleInProgress = false
    var facebookInprogress = false
    var appleInprogress = false
    var isWebViewLoaded = false

    lateinit var appledialog: Dialog
//    lateinit var appleSignSupport: AppleSignSupport

    private val logTag = "LoginFragment"

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }


    //treinetic.macbook.002@gmail.com Developer20190716

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        mainView = binding.root
        checkUpdateMsg()
        model = ViewModelProvider(requireActivity())[LoginViewModel::class.java]
        BUTTON_BACKROUND = resources.getDrawable(R.drawable.button_circular_shape)
        FB_BACKROUND = resources.getDrawable(R.drawable.fb_background)
        GOOGLE_BACKROUND = resources.getDrawable(R.drawable.google_background)
        APPLE_BACKROUND = resources.getDrawable(R.drawable.apple_background)
//        setPrivacyFonts()
//        setTermsFonts()

        setChangeListener()
        setButtonBkg(binding.btnLogin, BUTTON_BACKROUND)

        getLoginData()
        showException()
        getEditTextFields()
        printHashKey(requireContext())

        binding.signUp.setOnClickListener(this)
        binding.forgotPassword.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.btnFacebook.setOnClickListener(this)
        binding.btnGoogle.setOnClickListener(this)
        binding.btnApple.setOnClickListener(this)
        binding.privacy.setOnClickListener(this)
        binding.terms.setOnClickListener(this)
        binding.loginBackBtn.setOnClickListener(this)
        binding.btnFacebook.post {
            //            animateFbButtonInit()
        }
        setupBtns()
        if (isDebugMode()) {
//            mainView.email.setText("admin.shehan@gmail.com")
//            mainView.password.setText("Shehan12345")
        }

        return mainView
    }


    private fun checkUpdateMsg() {

        if (UpdateService.isDisplayHomeMsg) {
            return
        }

        UpdateService.instance.appUpdate?.update?.let { update ->
            try {
                val isHomeMsg: Boolean =
                    update.extras?.get(Update.IS_HOME_MESSAGE_DISPLAY).toString().toBoolean()

                if (isHomeMsg) {
                    UpdateService.isDisplayHomeMsg = true
                    context?.let { cContext ->
                        materialDialogs.getDialog(
                            context = cContext,
                            title = update.extras?.get(Update.HOME_MESSAGE_TITTLE).toString(),
                            message = update.extras?.get(Update.HOME_MESSAGE_BODY).toString(),
                            positiveText = "OK",
                            positiveClick = {
                                it.dismiss()
                            },
                            cancelable = true,
                            cancelTouchOutSide = true

                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    logTag,
                    "$e\nError occurred when show dialog message in HomeFragment"
                )
                e.printStackTrace()
            }

        }

    }

    private fun setupBtns() {
        binding.fbLoadingBtn.isClickable = false
        binding.googleLoadingBtn.isClickable = false
        binding.appleLoadingBtn.isClickable = false
        setButtonBkg(binding.fbLoadingBtn, FB_BACKROUND)
        setButtonBkg(binding.googleLoadingBtn, GOOGLE_BACKROUND)
    }

    private fun getEditTextFields() {
        filedList = mutableListOf(
            binding.email,
            binding.password
        )
    }

    private inner class MyTextWatcher(val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun afterTextChanged(editable: Editable) {
            when (view) {
//                mainView.emailLayout -> {
//                    validateEmail(editable.toString())
//                }
//                else -> {
//
//                }
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (!FiledValidate.isEmailValid(email)) {
            binding.email.error = resources.getString(R.string.not_email)
            return false
        }
        return true
    }

    private fun setChangeListener() {
        //mainView.email.addTextChangedListener(MyTextWatcher(mainView.emailLayout))

    }

    private fun showPayNowBottomSheet() {
        var bottomDialog = BottomSheetBaseDialog()
        bottomDialog.isCancelable = false
        fragmentManager?.let { bottomDialog.show(it, "BottomSheetFragment") }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.signUp -> {
                if (isShowInternetError()) return
                FragmentNavigation.getInstance()
                    .startRegisterFragment(requireFragmentManager(), R.id.login_fragment_view)
            }
            binding.forgotPassword -> {
                FragmentNavigation.getInstance()
                    .startPasswordFragment(requireFragmentManager(), R.id.login_fragment_view)
            }
            binding.btnLogin -> {
                Keyboard.getInstance().hideKeyboard(mainView)
                if (isShowInternetError()) return
                binding.btnLogin.startAnimation()
                binding.btnGoogle.isClickable = false
                binding.btnFacebook.isClickable = false
                binding.btnApple.isClickable = false
                getInputData()
            }
            binding.btnFacebook -> {
//                if (true) {
//                animateFacebookBtn()
//                    return
//                }
                if (isShowInternetError()) return
                if (googleInProgress || facebookInprogress || appleInprogress) return
                binding.btnGoogle.isClickable = false
                binding.btnLogin.isClickable = false
                binding.btnApple.isClickable = false

                facebookLogin()
            }
            binding.btnGoogle -> {
//                if (true) {
//                animateGoogleBtn()
//                }
                if (googleInProgress || facebookInprogress || appleInprogress) return
                if (isShowInternetError()) return
                binding.btnLogin.isClickable = false
                binding.btnFacebook.isClickable = false
                binding.btnApple.isClickable = false

                gPlusLogin()
            }
            binding.btnApple -> {

                if (googleInProgress || facebookInprogress || appleInprogress) return
                if (isShowInternetError()) return
                binding.btnLogin.isClickable = false
                binding.btnFacebook.isClickable = false
                binding.btnGoogle.isClickable = false

                signInWithApple()
            }
            binding.privacy -> {
                openPrivacyView()
            }
            binding.terms -> {
                openTermsView()
            }
            binding.loginBackBtn -> {
                activity?.onBackPressed()
            }

        }

    }


    fun isShowInternetError(): Boolean {
        if (!isConnected) {
            showErrorSnackBar(mainView, "Not connected to internet")
            return true
        }
        return false
    }

    private fun openPrivacyView() {
        TermsAndPrivacyActivity.show(requireContext(), "https://gurulugomi.lk/privacy-policy.html")


    }

    private fun openTermsView() {
        TermsAndPrivacyActivity.show(requireContext(), "https://gurulugomi.lk/tnc.html")
    }

    private fun startMainActivity() {
//        val intent = Intent(context, MainActivity::class.java)
//        startActivity(intent)

        finishLoginActivity()

    }


    private fun getInputData() {
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val deviceID = DeviceInformation.getInstance().getDeviceId()
        val deviceName = DeviceName.getDeviceName()
        normalLogin(email, password, deviceID, deviceName)
    }

    private fun normalLogin(email: String, pwd: String, id: String, name: String) {
        var noEmptyField = 0
        filedList.forEach { editText: EditText ->
            if (checkFieldEmpty(editText)) {
                noEmptyField++
                editText.error = resources.getString(R.string.required)
            }
        }

        if (noEmptyField == NO_EMPTY) {

            if (!validateEmail(binding.email.text.toString())) {
                binding.email.error = resources.getString(R.string.not_email)

                binding.btnLogin.revertAnimation()
                enableButtons()
                setButtonBkg(binding.btnLogin, BUTTON_BACKROUND)

            } else {
                model.normalLogin(email, pwd, id, name)
            }
        } else {
            binding.btnLogin.revertAnimation()
            enableButtons()
            setButtonBkg(binding.btnLogin, BUTTON_BACKROUND)
        }


    }

    private fun getLoginData() {
        model.getInitLogin().observe(viewLifecycleOwner, Observer { t ->
            t?.let {
                //                hideLoading()
//                mainView.btnLogin.revertAnimation()
                enableButtons()
//                setButtonBkg(mainView.btnLogin, BUTTON_BACKROUND)
                startMainActivity()
            }

        })
    }

    private fun checkFieldEmpty(view: EditText): Boolean {
        return view.text.isEmpty()
    }

    private fun facebookLogin() {
        googleInProgress = false
        facebookInprogress = true
        appleInprogress = false
        animateFacebookBtn()
        model.fBLogin(requireActivity() as AppCompatActivity)

        enableButtons()

    }

    private fun gPlusLogin() {
        googleInProgress = true
        facebookInprogress = false
        appleInprogress = false
        animateGoogleBtn()
        model.gPlusLogin(requireActivity() as AppCompatActivity)

    }


    private fun signInWithApple() {

        appleInprogress = true
        googleInProgress = false
        facebookInprogress = false

        animateAppleBtn()

        setupAppleWebviewDialog(AppleLoginService.getAuthUrl())


    }


    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    fun setupAppleWebviewDialog(url: String) {

        AppleWebViewClient.activity = requireActivity() as LoginActivity

        appledialog = Dialog(mainView.context)
        val webView = WebView(mainView.context)
        var webViewClient = AppleWebViewClient()

        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.settings.javaScriptEnabled = true

        webViewClient.clearHistory()
        webViewClient.onFinished = {
            isWebViewLoaded = true
            Log.d(logTag, "on page loaded!")
            if (!appledialog.isShowing && it == url) {
                appledialog.show()
            }
        }

        webView.addJavascriptInterface(
            AppleWebViewInterface.getInstance().apply {
                onTokenReceived = {
                    isWebViewLoaded = false
                    Log.d(logTag, "Token call back $it")
                    model.appleLogin(requireActivity() as AppCompatActivity, it)
                    appledialog.dismiss()
                }
                onSignFailed = {
                    isWebViewLoaded = false
                    Log.e(logTag, "sign failed")
                    appledialog.dismiss()
                    showMessageSnackBar(mainView, "Apple Sign in failed!")
                    isWebViewLoaded = false
                    hideLoading()
                    enableButtons()
                }
            }, "AppleLogin"
        )

        appledialog.setOnCancelListener {
            isWebViewLoaded = false
            hideLoading()
            enableButtons()
        }



        webView.loadUrl(url)

        webView.webViewClient = webViewClient

        //webView.reload()

        appledialog.setContentView(webView)


    }

    fun finishLoginActivity() {
        val returnIntent = Intent()
        returnIntent.putExtra("data", Contants.SUCCESS_CALLBACK)
        activity?.setResult(Activity.RESULT_OK, returnIntent)
        activity?.finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GOOGLE_LOGIN_REQUEST_CODE) {
            GPlusLoginService.getInstance().OnActivityResult(requestCode, resultCode, data)
            return
        }
        FacebookLoginService.getInstance().OnActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun printHashKey(context: Context) {
        try {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.e(logTag, "KEY hash : $hashKey")
            }

        } catch (e: Exception) {
            Log.e(logTag, "ERROR IN GETTING KEY HASH")
            e.printStackTrace()
        }
    }

    private fun showException() {
        model.getNetException().observe(viewLifecycleOwner, Observer { t ->

            t?.let {
                binding.btnLogin.revertAnimation()
                setButtonBkg(binding.btnLogin, BUTTON_BACKROUND)
                enableButtons()
                hideLoading()
                when {
                    it.code == INVALID_CREDENITAIL ->
                        showMessageSnackBar(
                            mainView,
                            resources.getString(R.string.invalid_credential)
                        )

                    it.code == USER_NOT_ACTIVATED -> {
                        if (it.message_id == "DEVICE_LIMIT_EXCEED") {
                            //show remove device view
                            FragmentNavigation.getInstance().startRemoveDeviceList(
                                requireFragmentManager(),
                                R.id.login_fragment_view,
                                UserService.getInstance().tempToken!!
                            )
                            /*   showMessageSnackBar(
                                   mainView,
                                   resources.getString(R.string.device_limit)
                               )*/
                            return@Observer
                        }
                        isIgnored(it.code, mutableListOf(it.code))
                        when {
                            it.message_id == "DEVICE_LIMIT_EXCEED" -> {
                                showMessageSnackBar(
                                    mainView,
                                    resources.getString(R.string.device_limit)
                                )
                            }
                            it.message_id == "USER_BANNED" -> {
                                showMessageSnackBar(
                                    mainView,
                                    "You have been banned. Contact Gurulugomi team for more details"
                                )
                            }
                            else -> {
                                showMessageSnackBar(
                                    mainView,
                                    resources.getString(R.string.user_not_activated)
                                )
                            }
                        }


                    }
                    it.message_id == "FB_LOGIN_CANCEL" -> {
                        Log.d(logTag, it.message?:"Fb login cancel")
                    }
                    it.message_id == "GPLUS_CANCEL" -> {
                        Log.d(logTag, it.message?:"fplus login cencel")
                    }
                    isErrorHandled(it) -> {
                    }
                    else -> {
                        showMessageSnackBar(mainView, "Some thing went wrong")
                    }
                }
            }
        })

    }


    private fun enableButtons() {
        binding.btnFacebook.isClickable = true
        binding.btnGoogle.isClickable = true
        binding.btnLogin.isClickable = true
        binding.btnApple.isClickable = true

    }

    fun animateFacebookBtn() {
        setButtonBkg(binding.fbLoadingBtn, FB_BACKROUND)
        binding.fbLoadingBtn.visibility = View.VISIBLE
        binding.btnFacebook.visibility = View.INVISIBLE
        binding.fbLoadingBtn.post {
            binding.fbLoadingBtn.startAnimation()
        }

    }

    fun animateGoogleBtn() {
        binding.googleLoadingBtn.visibility = View.VISIBLE
        binding.btnGoogle.visibility = View.INVISIBLE
        binding.googleLoadingBtn.post {
            binding.googleLoadingBtn.startAnimation()
        }
    }

    fun animateAppleBtn() {
        binding.appleLoadingBtn.visibility = View.VISIBLE
        binding.btnApple.visibility = View.INVISIBLE
        binding.appleLoadingBtn.post {
            binding.appleLoadingBtn.startAnimation()
        }
    }

    fun hideLoading() {
        if (googleInProgress) {

            binding.googleLoadingBtn.revertAnimation {
                binding.googleLoadingBtn.visibility = View.INVISIBLE
                binding.btnGoogle.visibility = View.VISIBLE
            }
            setButtonBkg(binding.googleLoadingBtn, GOOGLE_BACKROUND)
        }

        if (facebookInprogress) {
            binding.fbLoadingBtn.revertAnimation {
                binding.fbLoadingBtn.visibility = View.INVISIBLE
                binding.btnFacebook.visibility = View.VISIBLE
            }
            setButtonBkg(binding.fbLoadingBtn, FB_BACKROUND)

        }
        if (appleInprogress) {
            binding.appleLoadingBtn.revertAnimation {
                binding.appleLoadingBtn.visibility = View.INVISIBLE
                binding.btnApple.visibility = View.VISIBLE
            }

            setButtonBkg(binding.appleLoadingBtn, APPLE_BACKROUND)

        }
        googleInProgress = false
        facebookInprogress = false
        appleInprogress = false

    }

    private fun setButtonBkg(view: View, drawable: Drawable) {
        val button = view as CircularProgressButton
        button.background = drawable
    }


    private fun setTermsFonts() {
        val sb = SpannableStringBuilder(resources.getString(R.string.term_and_Condition))
        val bss = StyleSpan(android.graphics.Typeface.BOLD)
        sb.setSpan(bss, 33, 50, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        binding.terms.text = sb
    }

    private fun setPrivacyFonts() {
        val privacy = SpannableStringBuilder(resources.getString(R.string.privacy_policy))
        val iss = StyleSpan(android.graphics.Typeface.BOLD)
        privacy.setSpan(iss, 1, privacy.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        binding.privacy.text = " " + privacy
    }


}

