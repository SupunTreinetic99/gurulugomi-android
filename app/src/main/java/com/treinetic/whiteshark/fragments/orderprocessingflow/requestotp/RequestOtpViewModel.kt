package com.treinetic.whiteshark.fragments.orderprocessingflow.requestotp

import androidx.lifecycle.MutableLiveData
import com.treinetic.whiteshark.services.LoyaltyPointService
import com.treinetic.whiteshark.services.OrderService
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.viewmodels.BaseViewModel

class RequestOtpViewModel : BaseViewModel() {

    var success: MutableLiveData<String> = MutableLiveData()
    var error: MutableLiveData<String> = MutableLiveData()


    fun requestOtp(phone: String) {
        LoyaltyPointService.instance.requestOtp(
            phone = phone, orderId = OrderService.getInstance().currentOrder?.id!!,
            success = Service.Success { result ->
                success.postValue(result)
            },
            error = Service.Error { exception ->
                error.postValue(exception.message)
            }
        )
    }


}