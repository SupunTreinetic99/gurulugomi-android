package com.treinetic.whiteshark.util

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.treinetic.whiteshark.MyApp.context

class Keyboard {

    companion object {
        private val newInstance = Keyboard()
        fun getInstance(): Keyboard {
            return newInstance
        }
    }

    fun hideKeyboard(view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}