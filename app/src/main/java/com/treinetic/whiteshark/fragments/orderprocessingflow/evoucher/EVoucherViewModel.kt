package com.treinetic.whiteshark.fragments.orderprocessingflow.evoucher

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Order
import com.treinetic.whiteshark.services.OrderService
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.viewmodels.BaseViewModel

class EVoucherViewModel : BaseViewModel() {

    var logTag = "AddPromoViewModel"
    var success: MutableLiveData<Order> = MutableLiveData()
    var exception: MutableLiveData<NetException> = MutableLiveData()


    fun applyEVoucher(code: String) {

        OrderService.getInstance().applyEVoucher(
            code = code, orderId = OrderService.getInstance().currentOrder?.id!!,
            success = Service.Success { result ->
                Log.d(logTag, "Apply E Voucher success")
                success.postValue(result)
            }, error = Service.Error { exception ->
                exception.printe(logTag)
                this.exception.postValue(exception)

            }

        )


    }


}