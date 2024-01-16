package com.treinetic.whiteshark.fragments.register


import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.TermsAndPrivacyActivity
import com.treinetic.whiteshark.databinding.FragmentPublishersBinding
import com.treinetic.whiteshark.databinding.FragmentRegisterBinding
import com.treinetic.whiteshark.dialog.MaterialDialogs
import com.treinetic.whiteshark.dialog.ViewDialog
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.User
import com.treinetic.whiteshark.util.FiledValidate

class RegisterFragment : BaseFragment(), View.OnClickListener {

    private var _binding : FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var model: RegisterViewModel
    private var isValidate = false
    private lateinit var filedList: MutableList<EditText>
    private val NO_EMPTY = 0
    private lateinit var BUTTON_BACKROUND: Drawable
    private lateinit var viewDialog: ViewDialog

    companion object {
        fun newInstance(): RegisterFragment {
            return RegisterFragment()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[RegisterViewModel::class.java]
        BUTTON_BACKROUND = ContextCompat.getDrawable(requireContext(), R.drawable.button_circular_shape)!!
        viewDialog = ViewDialog()
        setButtonBkg(binding.btnRegister, BUTTON_BACKROUND)
        showException()
        getRegisterUser()
        getEditTextFields()
        binding.btnRegister.setOnClickListener(this)
        binding.rPrivacy.setOnClickListener(this)
        binding.signIn.setOnClickListener(this)
        binding.rTerms.setOnClickListener(this)
        binding.regBackBtn.setOnClickListener(this)
        // setChangeListener()
        return mainView
    }

    private fun getEditTextFields() {
        filedList = mutableListOf(
            binding.registerEmail,
            binding.registerPassword,
            binding.name,
            binding.confirmPassword
        )
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnRegister -> {
                binding.btnRegister.startAnimation()
                getInputData()
            }
            binding.rPrivacy -> {
                openPrivacyView()
            }
            binding.rTerms -> {
                openTermsView()
            }
            binding.signIn -> {
                FragmentNavigation.getInstance()
                    .startLoginFragment(requireFragmentManager(), R.id.login_fragment_view)
            }
            binding.regBackBtn -> {
                activity?.onBackPressed()
            }
        }
    }

    private fun openPrivacyView() {
        TermsAndPrivacyActivity.show(requireContext(), "http://gurulugomi.lk/privacy-policy.html")
    }

    private fun openTermsView() {
        TermsAndPrivacyActivity.show(requireContext(), "http://gurulugomi.lk/tnc.html")
    }

    private fun setChangeListener() {
//        mainView.registerEmail.addTextChangedListener(MyTextWatcher(mainView.emailLayoutR))
//        mainView.name.addTextChangedListener(MyTextWatcher(mainView.nameLayout))
//        mainView.registerPassword.addTextChangedListener(MyTextWatcher(mainView.passwordLayoutR))
//        mainView.confirmPassword.addTextChangedListener(MyTextWatcher(mainView.confirmPasswordL))
    }

    private inner class MyTextWatcher(val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun afterTextChanged(editable: Editable) {
            when (view) {
//                mainView.emailLayoutR -> {
//                    validateEmail(editable.toString())
//                }
//                mainView.nameLayout -> {
//                    validateName()
//                }
//                mainView.passwordLayoutR -> {
//                    validatePassword()
//                }
//                mainView.confirmPasswordL -> {
//
//                }
//                else -> {
//
//                }
            }
        }
    }

    private fun validateName(): Boolean {
        if (binding.name.text.toString().length > 50) {
            binding.name.error = resources.getString(R.string.invalid_name_length)
            return false
        } else {
            return true
        }
    }

    private fun validatePassword(): Boolean {
        if (binding.registerPassword.text.toString().length < 8) {
            binding.registerPassword.error = resources.getString(R.string.invalid_password)
            return false
        } else {
            return true
        }

    }

    private fun validateEmail(email: String): Boolean {
        return if (!FiledValidate.isEmailValid(email)) {
            binding.registerEmail.error = resources.getString(R.string.not_email)
            isValidate = false
            false
        } else {
            isValidate = true
            true
        }
    }

    private fun getInputData() {
        val email = binding.registerEmail.text.toString()
        val name = binding.name.text.toString()
        val password = binding.registerPassword.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()

        var noEmptyField = 0
        filedList.forEach { editText: EditText ->
            if (checkFieldEmpty(editText)) {
                noEmptyField++
                editText.error = resources.getString(R.string.required)
            }
        }

        if (noEmptyField == NO_EMPTY) {

            if (!validateName()) {
            } else if (password != confirmPassword) {
                binding.registerPassword.error = resources.getString(R.string.not_march)
                binding.confirmPassword.error = resources.getString(R.string.not_march)

            } else if (!validateEmail(binding.registerEmail.text.toString())) {
                binding.registerEmail.error = resources.getString(R.string.invalid_email)
            } else if (!validatePassword()) {

            } else {
                val newUser =
                    User(email = email, name = name, password = password, user_id = "")
                sendUserData(newUser)
                binding.btnRegister.startAnimation()
                return
            }
        }

        binding.btnRegister.revertAnimation()
        setButtonBkg(binding.btnRegister, BUTTON_BACKROUND)
    }

    private fun checkFieldEmpty(view: EditText): Boolean {
        return view.text.isEmpty()
    }

    private fun sendUserData(user: User) {
        model.registerUser(user)

    }

    private fun getRegisterUser() {
        model.getUser().observe(viewLifecycleOwner, Observer { t ->
            t?.let {
                binding.btnRegister.revertAnimation()
                //val viewDialog = ViewDialog()
                viewDialog.showDialog(requireContext(), "Hello")
                viewDialog.btnClick = {
                    FragmentNavigation.getInstance().startLoginFragment(
                        requireFragmentManager(), R.id.login_fragment_view
                    )
                }
            }
        })
    }

    private fun showException() {
        model.getNetException().observe(viewLifecycleOwner, Observer { t ->

            t?.let {
                binding.btnRegister.revertAnimation()
                if (isErrorHandled(it)) {
                } else {
                    when (it.message_id) {
                        "ALREADY_EXISTS" -> {
                            showMessageSnackBar(mainView, "Email is already exist.")
                        }
                        else -> {
                            showMessageSnackBar(mainView, "Something went wrong..")
                        }
                    }
                }
                model.clearException()
            }
        })
    }

    private fun setButtonBkg(view: View, drawable: Drawable) {
        val button = view as CircularProgressButton
        button.background = drawable
    }

    override fun onResume() {
        super.onResume()
        viewDialog.closeDialog()
    }

}
