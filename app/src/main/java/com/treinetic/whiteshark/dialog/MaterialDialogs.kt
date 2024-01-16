package com.treinetic.whiteshark.dialog

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog

class MaterialDialogs {

    var dialog: MaterialDialog? = null
    fun getConfirmDialog(context: Context, title: Int, message: Int): MaterialDialog {

        val titleStr = context.resources.getString(title)
        val messageStr = context.resources.getString(message)

        return getConfirmDialog(
            context, titleStr, messageStr
        )
    }

    fun getConfirmDialog(
        context: Context,
        title: String,
        message: String,
        positiveText: String = "Ok",
        positiveClick: (dialog: MaterialDialog) -> Unit = { it.dismiss() }
    ): MaterialDialog {
        val materialDialog = MaterialDialog(windowContext = context)
        materialDialog.apply {
            title(text = title)
            message(text = message)
            positiveButton(text = positiveText, click = {
                positiveClick(it)
            })
        }
        return materialDialog
    }

    fun customDialog(context: Context): MaterialDialog {
        val customDialog = MaterialDialog(windowContext = context)
        customDialog.apply {

        }
        return customDialog
    }

    fun dismissDialog(dialog: MaterialDialog) {
        if (dialog.isShowing) {
            dialog.dismiss()
        }

    }


    fun getDialog(
        context: Context,
        title: String,
        message: String,
        positiveText: String = "Ok",
        positiveClick: (dialog: MaterialDialog) -> Unit = { it.dismiss() },
        negativeText: String? = null,
        negativeClick: ((dialog: MaterialDialog) -> Unit)? = null,
        cancelable: Boolean = true,
        cancelTouchOutSide: Boolean = true
    ): MaterialDialog {
        dialog?.let { dismissDialog(it) }

        val materialDialog = MaterialDialog(windowContext = context)
        materialDialog.apply {
            title(text = title)
            message(text = message)
            positiveButton(text = positiveText, click = {
                positiveClick(it)
            })
            negativeText?.let {
                negativeButton(text = negativeText, click = { dilaog ->
                    negativeClick?.let { click -> click(dilaog) }
                })
            }

        }
        materialDialog.setCancelable(cancelable)
        materialDialog.setCanceledOnTouchOutside(cancelTouchOutSide)
        dialog = materialDialog
        return materialDialog

    }

    fun clear() {
        dialog?.let { dismissDialog(it) }
        this.dialog = null
    }

}