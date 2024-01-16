package com.treinetic.whiteshark.util

import android.app.Activity
import android.util.DisplayMetrics


class PixelConvert(val activity: Activity) {


    fun bookInRow(width: Float): Int {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        val deviceHeight = displayMetrics.heightPixels
        val deviceWidth = displayMetrics.widthPixels

        return (deviceWidth / width).toInt()

    }

}