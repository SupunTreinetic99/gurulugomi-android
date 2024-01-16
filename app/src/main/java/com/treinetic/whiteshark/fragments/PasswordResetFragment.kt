package com.treinetic.whiteshark.fragments


import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.FragmentEventDetailsBinding
import com.treinetic.whiteshark.databinding.FragmentPasswordResetBinding
import com.treinetic.whiteshark.dialog.MaterialDialogs
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.services.UserService
import com.treinetic.whiteshark.util.FiledValidate
import mehdi.sakout.fancybuttons.FancyButton


class PasswordResetFragment : BaseFragment(), View.OnClickListener {

    private var _binding : FragmentPasswordResetBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var BUTTON_BACKROUND: Drawable
    private lateinit var dialog: MaterialDialog

    companion object {
        fun newInstance(): PasswordResetFragment {
            return PasswordResetFragment()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPasswordResetBinding.inflate(inflater, container, false)
        mainView = binding.root
        setChangeListener()
        BUTTON_BACKROUND = resources.getDrawable(R.drawable.button_circular_shape)
        binding.registerBtn.setOnClickListener(this)
        binding.passwordReset.setOnClickListener(this)
        binding.resetBackBtn.setOnClickListener(this)
        return mainView
    }


    override fun onClick(view: View?) {
        when (view) {
            binding.registerBtn -> FragmentNavigation().startRegisterFragment(
                requireFragmentManager(), R.id.login_fragment_view
            )
            binding.passwordReset -> {
                hideKeyboard()
                binding.passwordReset.startAnimation()
                passwordReset()

            }
            binding.resetBackBtn -> {
                activity?.onBackPressed()
            }
            else -> {
                dialog.dismiss()
                FragmentNavigation.getInstance()
                    .startLoginFragment(requireFragmentManager(), R.id.login_fragment_view)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        setButtonBkg(binding.passwordReset, BUTTON_BACKROUND)
    }

    private fun passwordReset() {

        val resetEmail = binding.resetEmail.text.toString()

        when {
            resetEmail.isEmpty() -> {
                binding.resetEmail.error = resources.getString(R.string.email_require)
                resetAnimation()
            }
            !validateEmail(resetEmail) -> {
                resetAnimation()
            }
            else -> {
                sendEmailRequest(resetEmail)
            }
        }

    }

    private fun sendEmailRequest(resetEmail: String) {
        UserService.getInstance().sendReset(resetEmail, { result ->
            if (result.messageId == resources.getString(R.string.activation_email)) {
                getCustomDialog(resources.getString(R.string.activation_message))
            } else {
                getCustomDialog(resources.getString(R.string.reset_message))
            }

            resetAnimation()

        }, { exception ->
            when {
                exception.code == 400 -> {
                    getCustomDialog(resources.getString(R.string.reset_message))
                }
                isErrorHandled(exception) -> {

                }
                else -> {
                    exception.message?.let {
                        showErrorSnackBar(mainView, it)
                    }
                }
            }

            resetAnimation()
        })
    }

    private fun getCustomDialog(msg: String) {
        dialog = MaterialDialogs().customDialog(context!!).show {
            customView(R.layout.password_reset_success)
        }

        val customView = dialog.getCustomView()
        customView.findViewById<FancyButton>(R.id.loginBack).setOnClickListener(this)
        customView.findViewById<TextView>(R.id.message).text = msg
    }


    private fun resetAnimation() {
        binding.passwordReset.revertAnimation()
        setButtonBkg(binding.passwordReset, BUTTON_BACKROUND)
    }


    private fun setChangeListener() {
        //mainView.resetEmail.addTextChangedListener(MyTextWatcher(mainView.passwordResetLayout))

    }

    private inner class MyTextWatcher(val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun afterTextChanged(editable: Editable) {
            when (view) {
//                mainView.passwordResetLayout -> {
//                    validateEmail(editable.toString())
//                }
            }
        }
    }


    private fun validateEmail(email: String): Boolean = if (!FiledValidate.isEmailValid(email)) {
        binding.resetEmail.error = "Email Not Valid"
        false
    } else {
        true
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun setButtonBkg(view: View, drawable: Drawable) {
        val button = view as CircularProgressButton
        button.background = drawable
    }


}
