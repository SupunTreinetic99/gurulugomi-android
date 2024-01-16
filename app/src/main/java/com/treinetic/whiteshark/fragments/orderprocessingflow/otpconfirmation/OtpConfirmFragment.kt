package com.treinetic.whiteshark.fragments.orderprocessingflow.otpconfirmation

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.FragmentEVoucherBinding
import com.treinetic.whiteshark.databinding.FragmentOtpConfirmBottomSheetBinding
import com.treinetic.whiteshark.fragments.BaseFragment

class OtpConfirmFragment : BaseFragment() {

    private var _binding : FragmentOtpConfirmBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var BUTTON_BACKROUND: Drawable
    lateinit var model: OtpConfirmViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model = ViewModelProvider(requireActivity())[OtpConfirmViewModel::class.java]
        _binding = FragmentOtpConfirmBottomSheetBinding.inflate(inflater, container, false)
        mainView = binding.root
        setButtons()
        observeData()
        return mainView
    }

    private fun setButtons() {
        BUTTON_BACKROUND = resources.getDrawable(R.drawable.button_circular_shape)
        setButtonBkg(binding.confirmBtn, BUTTON_BACKROUND)

        binding.confirmBtn.setOnClickListener { confirmBtnClick() }
        binding.backBtn.setOnClickListener { backBtnClick() }
    }


    private fun setButtonBkg(view: View, drawable: Drawable) {
        val button = view as CircularProgressButton
        button.background = drawable
    }


    private fun backBtnClick() {
        FragmentNavigation.getInstance()
            .startRequestOtpFragment(requireFragmentManager(), R.id.bottomSheetContainer)
    }

    private fun confirmBtnClick() {
        var otp = binding.otp.text.toString()
        if (otp.isBlank()) {
            showMessageSnackBar(mainView, "OTP is invalid")
            return
        }
        disableClick()
        showLoading(true)
        model.validateOtp(otp)
    }


    private fun observeData() {
        model.success.observe(viewLifecycleOwner, Observer {
            it?.let {
                showLoading(false)
                FragmentNavigation.getInstance()
                    .startRedeemLoyaltyPointsFragment(
                        requireFragmentManager(),
                        R.id.bottomSheetContainer
                    )
                enableClick()
                model.success.postValue(null)
            }

        })

        model.error.observe(viewLifecycleOwner, Observer {
            it?.let {
                showLoading(false)
                enableClick()
                showMessageSnackBar(mainView, it)
                model.error.postValue(null)
            }
        })


    }

    fun showLoading(visible: Boolean) {
        if (visible) {
            binding.bottomLoading.visibility = View.VISIBLE
        } else {
            binding.bottomLoading.visibility = View.INVISIBLE
        }
    }


    private fun disableClick() {
        binding.backBtn.isClickable = false
        binding.confirmBtn.isClickable = false
        binding.otp.isEnabled = false
    }

    fun enableClick() {
        binding.backBtn.isClickable = true
        binding.confirmBtn.isClickable = true
        binding.otp.isEnabled = true
    }


}
