package com.treinetic.whiteshark.fragments.orderprocessingflow.requestotp

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
import com.treinetic.whiteshark.databinding.BottomSheetRedeemloyaltyBinding
import com.treinetic.whiteshark.databinding.BottomSheetRedeemloyaltyProcessBinding
import com.treinetic.whiteshark.fragments.BaseFragment

class RequestOtpFragment : BaseFragment() {

    private var _binding : BottomSheetRedeemloyaltyBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var BUTTON_BACKROUND: Drawable
    lateinit var model: RequestOtpViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model = ViewModelProvider(requireActivity())[RequestOtpViewModel::class.java]
        _binding = BottomSheetRedeemloyaltyBinding.inflate(inflater, container, false)
        mainView = binding.root
        setButtons()
        observeData()
        return mainView
    }


    private fun setButtons() {
        BUTTON_BACKROUND = resources.getDrawable(R.drawable.button_circular_shape)
        setButtonBkg(binding.conformBtn, BUTTON_BACKROUND)
        binding.backBtn.setOnClickListener { goBackClick() }
        binding.conformBtn.setOnClickListener { confirmClick() }
        binding.changeNumber.setOnClickListener { changeNumberClick() }
    }


    private fun setButtonBkg(view: View, drawable: Drawable) {
        val button = view as CircularProgressButton
        button.background = drawable
    }


    private fun goBackClick() {
        FragmentNavigation.getInstance()
            .startPayNowFragment(requireFragmentManager(), R.id.bottomSheetContainer)
    }


    private fun confirmClick() {

        var phone = binding.phone.text.toString()

        if (phone.isBlank()) {
            showMessageSnackBar(mainView, "Phone number invalid")
            return
        }
        disableClick()
        showLoading(true)
        model.requestOtp(phone)

    }

    private fun changeNumberClick() {
        FragmentNavigation.getInstance()
            .startChangeNumberFragment(requireFragmentManager(), R.id.bottomSheetContainer)
    }


    private fun observeData() {
        model.success.observe(viewLifecycleOwner, Observer {
            it?.let {
                showLoading(false)
                FragmentNavigation.getInstance()
                    .startOtpConfirmFragment(requireFragmentManager(), R.id.bottomSheetContainer)
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
        binding.conformBtn.isClickable = false
        binding.changeNumber.isClickable = false
    }

    fun enableClick() {
        binding.backBtn.isClickable = true
        binding.conformBtn.isClickable = true
        binding.changeNumber.isClickable = true
    }
}