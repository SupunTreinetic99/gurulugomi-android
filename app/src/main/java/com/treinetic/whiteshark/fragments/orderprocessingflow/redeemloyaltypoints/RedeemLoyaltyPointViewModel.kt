package com.treinetic.whiteshark.fragments.orderprocessingflow.redeemloyaltypoints

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.services.LoyaltyPointService
import com.treinetic.whiteshark.services.OrderService
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.util.extentions.toCurrency
import com.treinetic.whiteshark.viewmodels.BaseViewModel

class RedeemLoyaltyPointViewModel : BaseViewModel() {
    private val tag = "RedeemLoyaltyVM"
    var totalPoints = "0"
    var selectedPointValue: MutableLiveData<String> = MutableLiveData()

    init {
        selectedPointValue.value = "Rs. 0.00"
    }


    var addLoyaltyPointSuccess: MutableLiveData<String> = MutableLiveData()
    var addLoyaltyPointError: MutableLiveData<NetException> = MutableLiveData()


    fun setData() {

        LoyaltyPointService.instance.loyaltyData?.let { data ->
            totalPoints = data.points.toString()
        }

    }


    fun calculatePointsValue(points: Double) {

        LoyaltyPointService.instance.loyaltyData?.let { data ->
            Log.d(
                tag,
                "calculatePointsValue " +
                        "points : $points " +
                        "value : ${data.value}  " +
                        "total available: ${data.points}"
            )
            var price = (data.value * points).toCurrency("Rs")
            selectedPointValue.postValue(price)
            Log.d(tag, "selectedPointValue value: ${selectedPointValue.value}")
        }
    }


    fun addLoyaltyPoints(points: String) {
        LoyaltyPointService.instance.addLoyaltyPoints(
            points = points.toDouble(),
            orderId = OrderService.getInstance().currentOrder?.id!!,
            success = Service.Success { order ->
                addLoyaltyPointSuccess.postValue("success")
            },
            error = Service.Error { exception ->
                addLoyaltyPointError.postValue(exception)
            }
        )
    }

}