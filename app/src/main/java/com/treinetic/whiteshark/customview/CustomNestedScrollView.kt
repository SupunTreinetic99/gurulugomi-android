package com.treinetic.whiteshark.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.widget.NestedScrollView

class CustomNestedScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    var scrollChangedListener: ((view: CustomNestedScrollView, x: Int, y: Int, oldx: Int, oldy: Int) -> Unit)? =
        null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        scrollChangedListener?.let { it(this,l, t, oldl, oldt) }
    }
}