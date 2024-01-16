package com.treinetic.whiteshark.services

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.jaredrummler.android.device.DeviceName
import com.treinetic.whiteshark.constance.DeviceInformation
import com.treinetic.whiteshark.models.Login
import com.treinetic.whiteshark.network.Net
import java.util.*

class AppleLoginService {

    private var callbackManager: CallbackManager? = null
    private val appleToken: String? = null


    companion object {
        val CLIENT_ID = "com.treinetic.whiteshark.client"

        //        val REDIRECT_URI = "https://dev.gurulugomi.lk/api/v1.0/login/apple/callback"
        val REDIRECT_URI = Net.URL.APPLE_AUTH
        //.replace("http", "https")
        val SCOPE = "name%20email"

        val AUTHURL = "https://appleid.apple.com/auth/authorize"
        val TOKENURL = "https://appleid.apple.com/auth/token"
        var TAG: String = "AppleLoginService"
        val APPLE: String = "apple"


        fun getAuthUrl(): String {

            val state = UUID.randomUUID().toString()
            val appleAuthURLFull =
                AppleLoginService.AUTHURL + "?response_type=code&v=1.1.6&response_mode=form_post&client_id=" + CLIENT_ID + "&scope=" + SCOPE + "&state=" + state + "&redirect_uri=" + REDIRECT_URI
            return appleAuthURLFull
        }

        private var instance = AppleLoginService()
        fun getInstance(): AppleLoginService {
            return instance
        }
    }

    fun doLogin(
        token: String,
        activity: AppCompatActivity,
        success: Service.Success<Login>,
        error: Service.Error
    ) {


        if (callbackManager == null) {
            callbackManager = CallbackManager.Factory.create()
        }

        LoginManager.getInstance().logOut()


        val deviceId = DeviceInformation().getDeviceId()
        val deviceName = DeviceName.getDeviceName()
        UserService.getInstance().socialLogin(
            APPLE,
            token,
            deviceId,
            deviceName,
            success,
            error
        )

    }

    fun OnActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (callbackManager != null) {
            callbackManager!!.onActivityResult(requestCode, resultCode, data)
        } else {
            Log.e(TAG, "OnActivityResult callbackManager null")
        }
    }

    fun getToken(): String? {
        return appleToken
    }
}