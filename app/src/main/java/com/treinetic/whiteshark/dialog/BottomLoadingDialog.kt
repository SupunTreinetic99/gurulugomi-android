package com.treinetic.whiteshark.dialog

import android.content.Context
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.treinetic.whiteshark.R

/**
 * Created by Nuwan on 2019-04-24.
 */
class BottomLoadingDialog {


    fun getLoadingDialog(
        context: Context,
        titleText: String = "Processing",
        msg: String = "Please wait.Processing..."
    ): BottomSheetDialog {

        var dialog = getBottomDialog(context)
        val title = dialog.findViewById<TextView>(R.id.title)
        val message = dialog.findViewById<TextView>(R.id.message)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog

    }


    private fun getBottomDialog(context: Context): BottomSheetDialog {
        var dialog = BottomSheetDialog(context)
        dialog.setContentView(R.layout.bottom_loading_layout)
        return dialog
    }


}