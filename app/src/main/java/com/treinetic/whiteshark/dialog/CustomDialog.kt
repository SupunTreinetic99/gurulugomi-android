package com.treinetic.whiteshark.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.treinetic.whiteshark.R

class CustomDialog : DialogFragment() {

    var onClick: ((dialog: DialogFragment) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.dialog_tittle)

                .setItems(R.array.array_str) { dialog, which ->
                    onClick?.let {
                        it(this)
                    }

                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}