package com.treinetic.whiteshark.customview

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import androidx.annotation.Keep
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.LoginActivity
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.databinding.BottomLoginSheetBinding
import com.treinetic.whiteshark.databinding.NestedRecyclerCardBinding

@Keep
class BottomLoginSheet @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {

    val TAG: String = "BottomLoginSheet"
    lateinit var binding : BottomLoginSheetBinding
//    private val binding get() = _binding!!
//    lateinit var mainView: View
    private val fragmentManager = context as MainActivity
    private lateinit var BUTTON_BACKROUND: Drawable
    var onBtnClick: (() -> Unit)? = null

    init {
        try {
            binding = BottomLoginSheetBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )
            BUTTON_BACKROUND = resources.getDrawable(R.drawable.button_circular_shape)
            setButtonBkg(binding.btnLoginBottomSheet, BUTTON_BACKROUND)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.btnLoginBottomSheet.setOnClickListener(this)
        binding.btnBottomLoginSheetClose.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnLoginBottomSheet -> {
                dismiss()
                startLoginActivity()
            }
            binding.btnBottomLoginSheetClose -> {
                dismiss()
            }
        }
    }

    fun show() {
        this.visibility = View.VISIBLE
        slideUp(this)
    }

    fun dismiss() {
        this.visibility = View.GONE
        slideDown(this)
    }

    private fun startLoginActivity() {
        onBtnClick?.invoke()
    }

    private fun setButtonBkg(view: View, drawable: Drawable) {
        val button = view as CircularProgressButton
        button.background = drawable
    }

    fun slideUp(view: View) {
        val animate = TranslateAnimation(
            0F,
            0F,
            view.height.toFloat(),
            0F
        )
        animate.duration = 300
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    fun slideDown(view: View) {
        val animate =
            TranslateAnimation(
                0F,
                0F,
                0F,
                view.height.toFloat()
            )
        animate.duration = 300
        animate.fillAfter = true
        view.startAnimation(animate)
    }
}