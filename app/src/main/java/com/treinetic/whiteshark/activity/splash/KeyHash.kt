package com.treinetic.whiteshark.activity.splash

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import java.security.MessageDigest

fun SplashActivity.printHashKey() {
    try {
        var info: PackageInfo =
            getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES)
        info.signatures?.forEach {
            var md: MessageDigest = MessageDigest.getInstance("SHA")
            md.update(it.toByteArray())
            var hashKey: String = String(Base64.encode(md.digest(), 0))
            Log.i(TAG, "printHashKey() Hash Key: " + hashKey)
        }
    } catch (e: Exception) {
        Log.e(TAG, "printHashKey()", e)
    }
}

