package com.treinetic.whiteshark.activity.splash

import android.util.Log
import com.treinetic.whiteshark.BuildConfig
import com.treinetic.whiteshark.models.appupdate.AppUpdate
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.services.UpdateService


fun SplashActivity.checkUpdates() {
    UpdateService.instance.checkUpdates(
        { result ->
            Log.d(TAG, "checkUpdates success")
            processUpdate(result)
        }, { exception ->
            exception.printStackTrace()
            getInitData()
        })
}

fun SplashActivity.processUpdate(appUpdate: AppUpdate) {
    if (BuildConfig.BUILD_TYPE == "debug") {
        getInitData()
        return
    }
    if (appUpdate.hasCriticalUpdate()) {
        Log.e(TAG, "Has Critical update")
        showCriticalUpdate(appUpdate)
        return
    }
    if (appUpdate.hasUpdate()) {
        Log.d(TAG, "Has update")
        showUpdateAvailable(appUpdate)
        return
    }

    Log.d(TAG, "No Updates Available")
    getInitData()
}

fun SplashActivity.showCriticalUpdate(appUpdate: AppUpdate) {

    runOnUiThread {
        appUpdate.getMessage().let {
            materialDialogs.getDialog(
                context = this,
                title = "Critical Update Available",
                message = it,
                positiveText = "Update",
                positiveClick = {
                    openAppLink(it)
                },
                cancelable = false,
                cancelTouchOutSide = false
            ).show()
        }
    }
}

fun SplashActivity.showUpdateAvailable(appUpdate: AppUpdate) {

    runOnUiThread {
        materialDialogs.getDialog(
            context = this,
            title = "Update Available",
            message = appUpdate.getMessage(),
            positiveText = "Update",
            positiveClick = ::openAppLink,
            negativeText = "Cancel",
            negativeClick = {
                it.dismiss()
                getInitData()
            }
        ).show()
    }

}
