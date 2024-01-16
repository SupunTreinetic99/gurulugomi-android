package com.treinetic.whiteshark.fragments.library

import android.os.Handler
import android.os.Looper


fun MyLibraryFragment.hidePrepareDialog() {
    dialog?.let {
        if (dialog.isShowing) {
            Handler(Looper.getMainLooper()).post {
                dialog.dismiss()
            }

        }
    }
}