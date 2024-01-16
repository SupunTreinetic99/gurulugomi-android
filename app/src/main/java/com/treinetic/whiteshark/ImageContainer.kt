package com.treinetic.whiteshark

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.Keep
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

@Keep
class ImageContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


    fun createImageView(imageArray: List<String>, width: Int, height: Int, marginLeft: Int, marginRight: Int) {
        removeAllViews()
        for (i in imageArray) {
            val subView = createFrameLayout(width, height, marginLeft, marginRight)
            subView.addView(createImage(i))
            addView(subView)
        }


    }

    private fun createFrameLayout(width: Int, height: Int, marginLeft: Int, marginRight: Int): FrameLayout {

        val frameLayout = FrameLayout(context)

        frameLayout.layoutParams = getLayoutParams(width, height, marginLeft, marginRight)

        return frameLayout
    }

    private fun createImage(url: String): CircleImageView {
        val circleImage = CircleImageView(context)
        Glide.with(context).load(url).placeholder(R.drawable.placeholder_user).into(circleImage)
        return circleImage

    }

    private fun getLayoutParams(width: Int, height: Int, marginLeft: Int, marginRight: Int): ViewGroup.LayoutParams {

        val params = FrameLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params.width = width
        params.height = height

        params.marginStart = marginLeft
        params.marginEnd = marginRight

        return params

    }

}