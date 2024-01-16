package com.treinetic.whiteshark.dialog


import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R

class BottomSheetBaseDialog : BottomSheetDialogFragment() {

    lateinit var mainview: View
    var onDismissListener:((dialog:BottomSheetBaseDialog)->Unit)?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainview = inflater.inflate(R.layout.fragment_bottom_sheet, container, false)

        dialog!!.setOnShowListener(object : DialogInterface.OnShowListener {
            override fun onShow(dialog: DialogInterface) {
                val d: BottomSheetDialog = dialog as BottomSheetDialog
                val bottomSheetInternal: View =
                    d.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!
                BottomSheetBehavior.from(
                    bottomSheetInternal
                ).state = BottomSheetBehavior.STATE_EXPANDED
            }
        })

        FragmentNavigation.getInstance()
            .startPayNowFragment(childFragmentManager, R.id.bottomSheetContainer)



        return mainview
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.let { it(this) }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onDismissListener?.let { it(this) }
    }

}
