package com.treinetic.whiteshark.fragments.orderprocessingflow.evoucher


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.BottomSheetChangenumberBinding
import com.treinetic.whiteshark.databinding.FragmentEVoucherBinding
import com.treinetic.whiteshark.databinding.FragmentEventBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.util.extentions.getDrawable

class EVoucherFragment : BaseFragment() {

    private var _binding : FragmentEVoucherBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var buttonBackground: Drawable
    lateinit var model: EVoucherViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model = ViewModelProvider(requireActivity())[EVoucherViewModel::class.java]
        observeData()
        _binding = FragmentEVoucherBinding.inflate(inflater, container, false)
        mainView = binding.root
        setButtons()
        return mainView
    }


    private fun setButtons() {

        buttonBackground = R.drawable.button_circular_shape.getDrawable(requireContext())!!
        setButtonBkg(binding.applyBtn, buttonBackground)

        binding.applyBtn.setOnClickListener { applyBtnClick() }
        binding.backBtn.setOnClickListener { backBtnClick() }
    }


    private fun setButtonBkg(view: View, drawable: Drawable) {
        val button = view as CircularProgressButton
        button.background = drawable
    }


    private fun applyBtnClick() {
        val code: String = binding.code.text.toString()
        if (code.isBlank()) {
            showMessageSnackBar(mainView, "eVoucher code invalid")
            return
        }
        binding.loadingView.show()
        setViewInteraction(false)
        model.applyEVoucher(code)
    }

    private fun backBtnClick() {
        FragmentNavigation.getInstance()
            .startPayNowFragment(requireFragmentManager(), R.id.bottomSheetContainer)
    }

    private fun observeData() {
        model.success.observe(viewLifecycleOwner, Observer { order ->
            order?.let {
                binding.loadingView.hide()
                FragmentNavigation.getInstance()
                    .startPayNowFragment(requireFragmentManager(), R.id.bottomSheetContainer)
                model.success.postValue(null)
            }
        })

        model.exception.observe(viewLifecycleOwner, Observer { exception ->
            exception?.let {
                binding.loadingView.hide()
                setViewInteraction(true)
                val msg = it.message ?: "Something went wrong"
                showMessageSnackBar(mainView, msg)
                model.exception.postValue(null)
            }
        })
    }

    private fun setViewInteraction(enable: Boolean) {
        binding.backBtn.isClickable = enable
        binding.applyBtn.isClickable = enable
        binding.code.isEnabled = enable
    }


}