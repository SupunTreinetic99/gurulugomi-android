package com.treinetic.whiteshark.fragments.orderprocessingflow.otpconfirmation

import androidx.lifecycle.MutableLiveData
import com.treinetic.whiteshark.services.LoyaltyPointService
import com.treinetic.whiteshark.services.OrderService
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.viewmodels.BaseViewModel

class OtpConfirmViewModel : BaseViewModel() {


    var success: MutableLiveData<String> = MutableLiveData()
    var error: MutableLiveData<String> = MutableLiveData()


    fun validateOtp(otp: String) {
        LoyaltyPointService.instance.validateOtp(
            otp = otp, token = LoyaltyPointService.instance.token,
            success = Service.Success { result ->
                success.postValue("success")
            },
            error = Service.Error { exception ->
                error.postValue(exception.message)
            }
        )
    }
}