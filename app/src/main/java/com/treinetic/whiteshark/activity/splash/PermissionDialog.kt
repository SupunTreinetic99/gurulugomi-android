package com.treinetic.whiteshark.activity.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.treinetic.whiteshark.R


fun SplashActivity.checkPermissions() {

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        if (
/*        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED ||*/
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            showPermissionRatioanleDialog()
            return
        }
    }else{
        if (
/*        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED ||*/
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            showPermissionRatioanleDialog()
            return
        }
    }

    requestRuntimePermission()
}

fun SplashActivity.showPermissionRatioanleDialog() {

    var dialog = materialDialogs.getConfirmDialog(
        context = this,
        title = getString(R.string.permission_we_request),
        message = getString(R.string.permission_rationale_message),
        positiveClick = {
            it.dismiss()
            requestRuntimePermission()
        }
    )

    dialog.setCancelable(false)
    dialog.cancelOnTouchOutside(false)
    dialog.show()

}




