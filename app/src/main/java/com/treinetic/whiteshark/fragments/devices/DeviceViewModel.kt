package com.treinetic.whiteshark.fragments.devices


import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.treinetic.whiteshark.activity.LoginActivity
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.constance.DeviceInformation
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Device
import com.treinetic.whiteshark.network.Net
import com.treinetic.whiteshark.services.*

class DeviceViewModel : ViewModel() {

    private var deviceList: MutableLiveData<MutableList<Device>> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()

    fun getDeviceList(): MutableLiveData<MutableList<Device>> {
        return deviceList
    }

    fun getNetException(): MutableLiveData<NetException> {
        return netException
    }

    fun fetchDeviceList() {
        UserService.getInstance().fetchDeviceList(Service.Success { result ->
            deviceList.value = result

        }, Service.Error { exception ->
            netException.value = exception
        })
    }

    fun removeDevice(deviceId: String, id: String, context: Context) {
        UserService.getInstance().removeDevice(id, Service.Success { result ->
            deviceList.value = result
            if (DeviceInformation.getInstance().getDeviceId().equals(deviceId)) {
                logout(context)
            }

        }, Service.Error { exception ->
            netException.value = exception
        })
    }

    private fun logout(context: Context) {
        LoginManager.getInstance().logOut()
        BookService.getInstance().clear()
        UserService.getInstance().clear()
        HomeService.getInstance().clear()
        WishListService.getInstance().clear()
        OrderService.getInstance().clear()
        CartService.getInstance().clear()
        LocalStorageService.getInstance().removeCurrentUser()
        LocalStorageService.getInstance().saveToken(null)
        Net.setTOKEN(null)
        signOutGoogle(context)

        MainActivity.show(context = context)
    }

    private fun signOutGoogle(context: Context) {
        try {
            val gso: GoogleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()

            val googleSignInClient: GoogleSignInClient =
                GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut()

        } catch (e: Exception) {
            Log.e("DeviceViewModel", e.message?:"No message")
        }

    }

}