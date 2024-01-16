package com.treinetic.whiteshark.util.extentions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.treinetic.whiteshark.MyApp


fun Int.getDrawable(context: Context): Drawable? {
    return ContextCompat.getDrawable(context, this)
}

fun Context.getDrawable(resId: Int): Drawable? {
    return ContextCompat.getDrawable(this, resId)
}

fun Int.getString(): String = MyApp.getAppContext().getString(this)

