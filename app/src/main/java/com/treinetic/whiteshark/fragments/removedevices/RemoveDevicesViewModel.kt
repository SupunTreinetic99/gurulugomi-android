package com.treinetic.whiteshark.fragments.removedevices

import android.content.Context
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebook.login.LoginManager
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.constance.DeviceInformation
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Device
import com.treinetic.whiteshark.network.Net
import com.treinetic.whiteshark.services.*

class RemoveDevicesViewModel : ViewModel() {
    var deviceList: MutableLiveData<MutableList<Device>> = MutableLiveData()
    var deviceListForDelete: MutableLiveData<MutableList<Device>> = MutableLiveData()
    var netException: MutableLiveData<NetException> = MutableLiveData()
    var deviceAllList: MutableList<Device> = mutableListOf()

    var token: String? = null

    fun fetchDeviceList() {
        UserService.getInstance().fetchDeviceList({ result ->
            deviceList.postValue(result)
            deviceAllList.clear()
            deviceAllList = result
        }, { exception ->
            netException.value = exception
        }, token)
    }

    fun removeDevice(deviceId: String, id: String, context: Context) {
        UserService.getInstance().removeDevice(id, { result ->
            deviceListForDelete.postValue(result)
            deviceAllList.clear()
            deviceAllList = result
        }, { exception ->
            netException.value = exception
        }, token)
    }
}