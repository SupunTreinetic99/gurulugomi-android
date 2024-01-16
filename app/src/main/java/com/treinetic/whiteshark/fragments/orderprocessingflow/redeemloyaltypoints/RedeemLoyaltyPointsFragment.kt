package com.treinetic.whiteshark.fragments.orderprocessingflow.redeemloyaltypoints

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.BottomSheetPaynowBinding
import com.treinetic.whiteshark.databinding.BottomSheetRedeemloyaltyProcessBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.services.LoyaltyPointService
import com.treinetic.whiteshark.services.OrderService


class RedeemLoyaltyPointsFragment : BaseFragment() {

    private var _binding : BottomSheetRedeemloyaltyProcessBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var BUTTON_BACKROUND: Drawable
    lateinit var model: RedeemLoyaltyPointViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        model = ViewModelProvider(requireActivity())[RedeemLoyaltyPointViewModel::class.java]
        observeData()
        _binding = BottomSheetRedeemloyaltyProcessBinding.inflate(inflater, container, false)
//        val dataBinding: BottomSheetRedeemloyaltyProcessBinding =
//            DataBindingUtil.inflate(
//                inflater,
//                R.layout.bottom_sheet_redeemloyalty_process,
//                container,
//                false
//            )
//        mainview = inflater.inflate(R.layout.bottom_sheet_redeemloyalty_process, null, false)
        binding.lifecycleOwner = this
        mainView = binding.root
        binding.model = model
        setButtons()
        calculatePointValue()
        model.setData()
        return mainView
    }

    private fun setButtons() {
        BUTTON_BACKROUND =
            ContextCompat.getDrawable(requireContext(), R.drawable.button_circular_shape)!!
        setButtonBkg(binding.redeemBtn, BUTTON_BACKROUND)

        binding.redeemBtn.setOnClickListener { redeemBtnClick() }
        binding.backBtn.setOnClickListener { backBtnClick() }
    }

    private fun setButtonBkg(view: View, drawable: Drawable) {
        val button = view as CircularProgressButton
        button.background = drawable
    }


    fun calculatePointValue() {
        binding.points.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isBlank()) {
                    model.selectedPointValue.postValue("Rs 0.00")
                    return
                }
                model.calculatePointsValue(s.toString().toDouble())
            }

        }
        )
    }

    private fun redeemBtnClick() {
        val points = binding.points.text.toString()
        if (points.isBlank()) {
            showMessageSnackBar(mainView, "Loyalty points amount is Invalid")
            return
        }
        LoyaltyPointService.instance.loyaltyData?.let { it ->
            val loyaltyPrice = points.toDouble() * it.value
            Log.d("REDEEMView", "loyalty price ${loyaltyPrice}")
            OrderService.getInstance().currentOrder?.totalPaid?.let { paid ->
                Log.d("REDEEMView", "payable price ${paid}")
                if (loyaltyPrice > paid) {
                    showMessageSnackBar(
                        mainView,
                        "Loyalty points should be less than the order total"
                    )
                    return
                }
            }

        }

        showLoading(true)
        setViewInteraction(false)
        model.addLoyaltyPoints(points)
    }

    private fun backBtnClick() {
        FragmentNavigation.getInstance()
            .startPayNowFragment(requireFragmentManager(), R.id.bottomSheetContainer)
//            .startRequestOtpFragment(requireFragmentManager(), R.id.bottomSheetContainer)
    }


    private fun observeData() {
        model.addLoyaltyPointSuccess.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                showLoading(false)
                FragmentNavigation.getInstance()
                    .startPayNowFragment(requireFragmentManager(), R.id.bottomSheetContainer)
            }
            model.addLoyaltyPointSuccess.postValue(null)
        })
        model.addLoyaltyPointError.observe(viewLifecycleOwner, Observer { exception ->
            exception?.let {
                setViewInteraction(true)
                showLoading(false)
                showMessageSnackBar(mainView, it.message ?: "Something went wrong")
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

    fun setViewInteraction(enable: Boolean) {
        binding.backBtn.isClickable = enable
        binding.redeemBtn.isClickable = enable
        binding.points.isEnabled = enable
    }

}