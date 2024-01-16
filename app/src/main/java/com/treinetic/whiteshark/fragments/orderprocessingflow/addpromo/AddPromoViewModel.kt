package com.treinetic.whiteshark.fragments.orderprocessingflow.addpromo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Order
import com.treinetic.whiteshark.services.OrderService
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.viewmodels.BaseViewModel

class AddPromoViewModel : BaseViewModel() {
    var logTag = "AddPromoViewModel"
    var addPromoSuccess: MutableLiveData<Order> = MutableLiveData()
    var exception: MutableLiveData<NetException> = MutableLiveData()

    fun addPromoCode(promoCode: String, orderId: String) {

        OrderService.getInstance().addPromoCode(
            orderId = orderId,
            promoCode = promoCode,
            success = Service.Success { order ->
                Log.d(logTag, "Adding promo code Success")
                addPromoSuccess.postValue(order)
            },
            error = Service.Error { exception ->
                Log.e(logTag, "Error in Adding promo code")
                exception.printStackTrace()
                this.exception.postValue(exception)
            }
        )

    }


}
