package com.treinetic.whiteshark.fragments.orderprocessingflow.addpromo

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.BottomSheetAddPramocodeBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.services.OrderService


class AddPromoFragment : BaseFragment() {

    val logTag = "AddPromoFragment"
    private var _binding : BottomSheetAddPramocodeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var BUTTON_BACKROUND: Drawable
    lateinit var model: AddPromoViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetAddPramocodeBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[AddPromoViewModel::class.java]
        setButtons()
        observeData()
        return mainView
    }

    private fun setButtons() {
        ContextCompat.getDrawable(requireContext(), R.drawable.button_circular_shape)?.let {
            BUTTON_BACKROUND = it
        }
        setButtonBkg(binding.applyBtn, BUTTON_BACKROUND)
        binding.btnAddPromoBack.setOnClickListener { backBtnClick() }
        binding.applyBtn.setOnClickListener { addPromoCodeClick() }
    }

    private fun observeData() {
        model.addPromoSuccess.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (!requireActivity().isFinishing) {
                    fragmentManager?.popBackStack()
                    model.addPromoSuccess.postValue(null)
                }
                binding.loadingView.hide()
                enableButtons()
            }
        })
        model.exception.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.loadingView.hide()
                enableButtons()
                var message = "Add promo code failed"
                it.message?.let { msg ->
                    message = msg
                }
                showMessageSnackBar(mainView, message)
                model.exception.postValue(null)
            }
        })
    }


    private fun setButtonBkg(view: View, drawable: Drawable) {
        val button = view as CircularProgressButton
        button.background = drawable
    }


    private fun addPromoCodeClick() {
        val promoCode = binding.promoCode.text.toString()
        val orderId: String? = OrderService.getInstance().currentOrder?.id
        if (promoCode.isBlank()) {
            showMessageSnackBar(mainView, "Promo code not valid")
            return
        }
        if (orderId.isNullOrBlank()) {
            Log.e(logTag, "")
            showMessageSnackBar(mainView, "Order information not available")
            return
        }
        disableButtons()
        binding.loadingView.show()
        model.addPromoCode(
            promoCode = promoCode,
            orderId = orderId
        )
    }

    private fun backBtnClick() {
        FragmentNavigation.getInstance()
            .startPayNowFragment(requireFragmentManager(), R.id.bottomSheetContainer)
    }

    private fun disableButtons() {
        binding.applyBtn.isClickable = false
        binding.applyBtn.isClickable = false
    }

    private fun enableButtons() {
        binding.applyBtn.isClickable = true
        binding.applyBtn.isClickable = true
    }

}