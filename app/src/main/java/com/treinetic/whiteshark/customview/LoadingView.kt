package com.treinetic.whiteshark.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.KeyValueLayoutBinding
import com.treinetic.whiteshark.databinding.ProgressLoadingLayoutBinding

class LoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    private var binding : ProgressLoadingLayoutBinding

    init {
        binding = ProgressLoadingLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        binding.bottomLoading.alpha = 0f
    }


    fun show() {
        binding.bottomLoading.animate().alpha(1f).setDuration(350).start()
    }


    fun hide() {
        binding.bottomLoading.animate().alpha(0f).setDuration(350).start()
    }


}