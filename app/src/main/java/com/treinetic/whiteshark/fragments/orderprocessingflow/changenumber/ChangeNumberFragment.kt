package com.treinetic.whiteshark.fragments.orderprocessingflow.changenumber

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.BottomSheetChangenumberBinding

class ChangeNumberFragment : Fragment(), View.OnClickListener {

    private var _binding : BottomSheetChangenumberBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var BUTTON_BACKROUND: Drawable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = BottomSheetChangenumberBinding.inflate(inflater, container, false)
        mainView = binding.root
        BUTTON_BACKROUND = resources.getDrawable(R.drawable.button_circular_shape)
        setButtonBkg(binding.btnConfirmChangeNumberBottomSheet, BUTTON_BACKROUND)
        binding.btnConfirmChangeNumberBottomSheet.setOnClickListener(this)
        binding.btnChangeNumberBack.setOnClickListener(this)
        return mainView
    }


    private fun setButtonBkg(view: View, drawable: Drawable) {
        val button = view as CircularProgressButton
        button.background = drawable
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnChangeNumberBack -> {
                FragmentNavigation.getInstance()
                    .startRequestOtpFragment(requireFragmentManager(), R.id.bottomSheetContainer)
            }
            binding.btnConfirmChangeNumberBottomSheet -> {
                FragmentNavigation.getInstance()
                    .startOtpConfirmFragment(requireFragmentManager(), R.id.bottomSheetContainer)
            }

        }
    }
}