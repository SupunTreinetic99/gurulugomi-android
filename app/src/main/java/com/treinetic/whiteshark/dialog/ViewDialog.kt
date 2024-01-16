package com.treinetic.whiteshark.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.treinetic.whiteshark.R
import mehdi.sakout.fancybuttons.FancyButton


class ViewDialog {

    var btnClick: ((message: String) -> Unit)? = null
    var btnAccept: ((massage: String) -> Unit)? = null
    var btnCancel: ((massage: String) -> Unit)? = null
    var dialog : Dialog? = null

    fun showDialog(context: Context, msg: String) {
        dialog = Dialog(context)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.dialog)
        val dialogButton = dialog?.findViewById<FancyButton>(R.id.btn_dialog)
        dialogButton?.setOnClickListener {
            btnClick?.let {
                it("success")
                dialog?.dismiss()
            }
        }
        dialog?.show()
    }


    fun showLogOutDialog(context: Context, msg: String) {
        dialog = Dialog(context)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.logout_dialog)
        val dialogButtonCancel = dialog?.findViewById<FancyButton>(R.id.btn_cancel)
        val dialogButtonLogOut = dialog?.findViewById<FancyButton>(R.id.btn_logout)
        dialogButtonLogOut?.setOnClickListener {
            btnClick?.let {
                it("success")
                dialog?.dismiss()
            }
        }
        dialogButtonCancel?.setOnClickListener{
            btnCancel?.let {
                dialog?.dismiss()
            }
        }
        dialog?.show()
    }


    fun closeDialog() {
        if (dialog?.isShowing == true){
            dialog?.dismiss()
        }
    }

    fun confirmDialog(context: Context, tittle: String, subTitle: String) {

        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.confirm_dialog)
        val accept = dialog?.findViewById<Button>(R.id.btn_accept)
        val cancel = dialog?.findViewById<Button>(R.id.btn_cancel)
        val subTextText = dialog?.findViewById<TextView>(R.id.dialog_sub_title)
        val title = dialog?.findViewById<TextView>(R.id.dialog_title)
        title?.text = tittle
        subTextText?.text = subTitle

        cancel?.setOnClickListener { view ->
            btnCancel?.let {
                it("cancel")
            }

            dialog?.dismiss()

        }
        accept?.setOnClickListener { view ->
            btnAccept?.let {
                it("accept")
            }
            dialog?.dismiss()
        }

        dialog?.show()
    }


}