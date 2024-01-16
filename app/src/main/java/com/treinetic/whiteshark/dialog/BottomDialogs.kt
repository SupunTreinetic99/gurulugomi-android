package com.treinetic.whiteshark.dialog

import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.treinetic.whiteshark.R

/**
 * Created by Nuwan on 4/9/19.
 */
class BottomDialogs {


    fun getBookNotDownLoadedDialog(
        context: Context,
        titleText: String,
        messageText: String,
        btnAction: (dialog: BottomSheetDialog) -> Unit = { it.dismiss() }
    ): BottomSheetDialog {


        val dialog = getBottomDialog(context, R.layout.bottom_dialog_message_layout)
        val title = dialog.findViewById<TextView>(R.id.title)
        val message = dialog.findViewById<TextView>(R.id.message)
        val btn = dialog.findViewById<TextView>(R.id.btn)
        title?.setText(titleText)
        message?.setText(messageText)
        btn?.setOnClickListener { btnAction(dialog) }

        return dialog

    }


    fun getBottomDialog(context: Context, resId: Int): BottomSheetDialog {
        var dialog = BottomSheetDialog(context)
        dialog.setContentView(resId)
        return dialog
    }

    fun getConfirmDialog(
        context: Context,
        titleText: String,
        messageText: String,
        positiveText: String = "Ok",
        positiveAction: (dialog: BottomSheetDialog) -> Unit = { it.dismiss() },
        negativeText: String = "Cancel",
        negativeActionAction: (dialog: BottomSheetDialog) -> Unit = { it.dismiss() }
    ): BottomSheetDialog {

        val dialog = getBottomDialog(context, R.layout.bottom_dialog_confirm_layout)
        val title = dialog.findViewById<TextView>(R.id.title)
        val message = dialog.findViewById<TextView>(R.id.message)
        val okBtn = dialog.findViewById<TextView>(R.id.okBtn)
        val cancelBtn = dialog.findViewById<TextView>(R.id.cancelBtn)

        okBtn?.setText(positiveText)
        okBtn?.setOnClickListener { positiveAction(dialog) }

        cancelBtn?.setText(negativeText)
        cancelBtn?.setOnClickListener { negativeActionAction(dialog) }

        title?.setText(titleText)
        message?.setText(messageText)

        return dialog

    }


    fun getMessageDialog(
        context: Context,
        titleText: String,
        messageText: String,
        positiveText: String = "Ok",
        positiveAction: (dialog: BottomSheetDialog) -> Unit = { it.dismiss() }
    ): BottomSheetDialog {

        val dialog = getBottomDialog(context, R.layout.bottom_dialog_confirm_layout)
        val title = dialog.findViewById<TextView>(R.id.title)
        val message = dialog.findViewById<TextView>(R.id.message)
        val okBtn = dialog.findViewById<TextView>(R.id.okBtn)
        val cancelBtn = dialog.findViewById<TextView>(R.id.cancelBtn)

        cancelBtn?.visibility = View.GONE
        okBtn?.setText(positiveText)
        okBtn?.setOnClickListener { positiveAction(dialog) }

        title?.setText(titleText)
        message?.setText(messageText)
        return dialog
    }


}