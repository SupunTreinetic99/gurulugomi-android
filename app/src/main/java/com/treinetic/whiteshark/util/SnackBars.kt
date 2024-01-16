package com.treinetic.whiteshark.util

import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import de.mateware.snacky.Snacky


/**
 * Created by Nuwan on 2/15/19.
 */
class SnackBars {

    fun getSuccessSnack(view: View, msg: String): Snackbar {
        val successSnack = Snacky.builder()
            .setView(view)
            .setText(msg)
            .setDuration(Snacky.LENGTH_LONG)
            .success()
        successSnack.view.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        return successSnack
    }


    fun getErrorSnack(view: View, msg: String): Snackbar {
        val errorSnack = Snacky.builder()
            .setView(view)
            .setText(msg)
            .setDuration(Snacky.LENGTH_LONG)
            .error()

        errorSnack.view.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        return errorSnack
    }

    fun getMessageSnack(view: View, msg: String): Snackbar {
        val messageSnack = Snacky.builder()
            .setView(view)
            .setText(msg)
            .setDuration(Snacky.LENGTH_LONG)
            .build()
        messageSnack.view.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        return messageSnack
    }


}