package com.treinetic.whiteshark.util.extentions

import android.graphics.Paint
import android.widget.TextView
import androidx.core.content.ContextCompat


fun TextView.drawStrikeThrough() {
    this.paintFlags = this.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
}

fun TextView.setTextColorRes(colorRes: Int) {
    try {
        this.setTextColor(ContextCompat.getColor(this.context, colorRes))
    } catch (e: Exception) {
        e.printStackTrace()
    }


}
