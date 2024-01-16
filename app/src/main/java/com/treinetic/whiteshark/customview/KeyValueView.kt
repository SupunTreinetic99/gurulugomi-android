package com.treinetic.whiteshark.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.Keep
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.CustomRecyclerViewLayoutBinding
import com.treinetic.whiteshark.databinding.KeyValueLayoutBinding
import com.treinetic.whiteshark.models.CustomData

@Keep
class KeyValueView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var _binding : KeyValueLayoutBinding? = null
    private val binding get() = _binding!!
    lateinit var mainView: View

    init {
        orientation = VERTICAL
    }

    var data: ArrayList<CustomData> = arrayListOf()
        set(value) {
            field = value
            updateUi()
        }

    fun updateUi() {
        removeAllViews()
        data.forEach {
            _binding = KeyValueLayoutBinding.inflate(
                LayoutInflater.from(context),
                this,
                false
            )
            mainView= binding.root
//            val row = LayoutInflater.from(context).inflate(R.layout.key_value_layout, this, false)
            binding.key.text = it.key
            binding.value.text = it.value
            addView(mainView)
        }

    }

}