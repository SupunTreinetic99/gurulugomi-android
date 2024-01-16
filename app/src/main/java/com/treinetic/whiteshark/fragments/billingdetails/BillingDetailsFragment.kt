package com.treinetic.whiteshark.fragments.billingdetails


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.hbb20.CCPCountry
import com.hbb20.CountryCodePicker
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.databinding.FragmentBillingDetailsBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.BillingDetails
import com.treinetic.whiteshark.services.UserService
import com.treinetic.whiteshark.util.FiledValidate

class BillingDetailsFragment : BaseFragment(), View.OnClickListener {

    private var logTag: String = "BillingDetails"
    private var _binding : FragmentBillingDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var model: BillingViewModel
    private lateinit var filedList: MutableList<EditText>
    private val NO_EMPTY = 0
    private lateinit var countryCodePicker: CountryCodePicker

    companion
    object {
        fun newInstance(): BillingDetailsFragment {
            return BillingDetailsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentBillingDetailsBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[BillingViewModel::class.java]
        binding.btnSave.setOnClickListener(this)
        showException()

        getEditTextFields()

        getBillingDetails()

        setHasOptionsMenu(true)

        setupToolBar()

        setChangeListener()

        return mainView
    }

    private fun getEditTextFields() {
        filedList = mutableListOf(
            binding.inputFirstName,
            binding.inputLastName,
            binding.inputEmail,
            binding.inputContact
//            mainView.inputAddress1,
//            mainView.inputCity
//            mainView.inputAddress2,
//            mainView.inputCountry,
//            mainView.inputZipCode
        )
    }

    private fun setChangeListener() {

        binding.inputFirstName.addTextChangedListener(MyTextWatcher(binding.layoutFirstName))
        binding.inputLastName.addTextChangedListener(MyTextWatcher(binding.layoutLastName))
        binding.inputEmail.addTextChangedListener(MyTextWatcher(binding.layoutEmail))
        binding.inputContact.addTextChangedListener(MyTextWatcher(binding.layoutContact))
//        mainView.inputAddress1.addTextChangedListener(MyTextWatcher(mainView.layoutAddress1))
//        mainView.inputCity.addTextChangedListener(MyTextWatcher(mainView.layoutCity))
//        mainView.inputCountry.addTextChangedListener(MyTextWatcher(mainView.layoutCountry))

    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnSave -> setBillingDetails()
        }

    }

    private inner class MyTextWatcher(val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun afterTextChanged(editable: Editable) {
            when (view) {
                binding.layoutEmail -> {
                    validateEmail(editable.toString())
                }
                binding.layoutContact -> {
                    validateMobile(editable.toString())
                }
                else -> {

                }
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (!FiledValidate.isEmailValid(email)) {
            binding.inputEmail.error = "Email Not Valid"
            return false
        } else {
            return true
        }
    }

    private fun validateMobile(mobile: String): Boolean {
        if (!FiledValidate.isMobile(mobile)) {
            binding.inputContact.error = "Number Not Valid "
            return false
        } else {
            return true
        }
    }

    private fun checkFieldEmpty(view: EditText): Boolean {
        return view.text.isEmpty()
    }

    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = resources.getString(R.string.billing_details)
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

    }

    private fun getBillingDetails() {
        model.getBillingDetails().observe(viewLifecycleOwner, Observer { t ->
            showBillingDetails(billingDetails = t)
        })
    }

    private fun showBillingDetails(billingDetails: BillingDetails) {
        binding.inputFirstName.setText(billingDetails.firstName)
        binding.inputLastName.setText(billingDetails.lastName)
        binding.inputEmail.setText(billingDetails.email)
        binding.inputContact.setText(UserService.getInstance().getUser()?.contact_number)
//        mainView.inputAddress1.setText(billingDetails.address1)
//        mainView.inputAddress2.setText(billingDetails.address2)
//        mainView.inputCity.setText(billingDetails.addressCity)
//        mainView.inputCountry.setText(billingDetails.country)

//        if (billingDetails.country != null) {
//            setSelectedCountry(billingDetails.country)
//        }
        if (billingDetails.countryCode != null) {
            setSelectedCountryCode(billingDetails.countryCode)
        }


//        mainView.inputZipCode.setText(billingDetails.zipCode)

    }

    private fun setBillingDetails() {
        var noEmtyFiled = 0
        val billingObj = BillingDetails(
            "", "",
            "", "", ""
        )
        billingObj.apply {
            firstName = binding.inputFirstName.text.toString()
            lastName = binding.inputLastName.text.toString()
            email = binding.inputEmail.text.toString()
            contact = binding.inputContact.text.toString()
//            address1 = mainView.inputAddress1.text.toString()
//            addressCity = mainView.inputCity.text.toString()
//            country = mainView.inputCountry.text.toString()
//            country = getCountry()
            countryCode = "+" + binding.countryCodePicker.selectedCountryCode
//            address2 = mainView.inputAddress2.text.toString()
//            zipCode = mainView.inputZipCode.text.toString()
        }

        var json: String = Gson().toJson(billingObj)
        Log.d(logTag, "billing json")
        Log.d(logTag, json)

        filedList.forEach { editText: EditText ->
            if (checkFieldEmpty(editText)) {
                noEmtyFiled++
                editText.error = resources.getString(R.string.required)
                return
            }
        }

        when {
            noEmtyFiled != NO_EMPTY -> {

            }

            !validateEmail(billingObj.email) -> {

            }
            !validateMobile(billingObj.contact) -> {

            }
            else -> {
                UserService.getInstance().getUser()?.contact_number =
                    binding.inputContact.text.toString()
                UserService.getInstance().getUser()?.country_code =
                    "+" + binding.countryCodePicker.selectedCountryCode
                passToViewModel(billingObj)
            }

        }
    }

    private fun passToViewModel(billingData: BillingDetails?) {
        billingData?.let {
            model.addBillingDetails(billingData, requireFragmentManager())
            showSuccessSnackBar(mainView, resources.getString(R.string.billing_details_success))
        }

    }

    private fun showException() {
        model.getNetException().observe(viewLifecycleOwner, Observer { t ->
            t?.let {
                if (isErrorHandled(it)) {
                } else {
                    t.message?.let {
                        showErrorSnackBar(mainView, getString(R.string.error_msg))
                    }

                }
            }
        })

    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        setOptionMenuVisibility(
            menu,
            false,
            false,
            false,
            false,
            false
        )


    }

    fun getPhoneNumber(): String {
        return binding.countryCodePicker.selectedCountryCode + binding.inputContact.text.toString()
    }

//    fun getCountry(): String {
//        return mainView.countryPicker.selectedCountryName
//    }

    fun setSelectedCountry(country: String) {
        var formattedCountry = country.toLowerCase()
        var c = CCPCountry.getLibraryMasterCountriesEnglish().find {
            formattedCountry == it.name.toLowerCase()
        } ?: return

//        mainView.countryPicker.post {
//            mainView.countryPicker.setDefaultCountryUsingNameCode(c.nameCode)
//            mainView.countryPicker.resetToDefaultCountry()
//        }
    }

    fun setSelectedCountryCode(countryCode: String?) {
//        countryCode ?: return
        var formattedCode = countryCode?.replace("+", "")
        var c = CCPCountry.getLibraryMasterCountriesEnglish().find {
            formattedCode == it.phoneCode
        } ?: return
        binding.countryCodePicker.post {
            binding.countryCodePicker.setDefaultCountryUsingNameCode(c.nameCode)
            binding.countryCodePicker.resetToDefaultCountry()
        }

    }

}
