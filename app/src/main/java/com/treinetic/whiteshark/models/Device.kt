package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.treinetic.whiteshark.constance.DeviceInformation
@Keep
data class Device(
    @SerializedName("id")
    var id: String,
    @SerializedName("user_customer_user_id")
    var userId: String,
    @SerializedName("device_id")
    var deviceId: String,
    @SerializedName("device_name")
    var deviceName: String,
    @SerializedName("last_logged_at")
    var lastLogged: String,
    @SerializedName("status")
    var status: String,
    var isThisDevice: Boolean = false
) {
    fun isCurrentDeivce() {
        val currentId = DeviceInformation().getDeviceId()
        if (currentId == deviceId) {
            isThisDevice = true
        }

    }

}

