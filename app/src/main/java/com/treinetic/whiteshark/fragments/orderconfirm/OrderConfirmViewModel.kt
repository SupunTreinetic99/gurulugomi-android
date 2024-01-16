package com.treinetic.whiteshark.fragments.orderconfirm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.BillingDetails
import com.treinetic.whiteshark.models.Order
import com.treinetic.whiteshark.models.PaymentData
import com.treinetic.whiteshark.services.CartService
import com.treinetic.whiteshark.services.OrderService
import com.treinetic.whiteshark.services.PaymentService
import com.treinetic.whiteshark.services.Service

class OrderConfirmViewModel : ViewModel() {

    var order: MutableLiveData<Order> = MutableLiveData()
    var billing: MutableLiveData<BillingDetails> = MutableLiveData()
    var cartService: CartService = CartService.getInstance()
    var orderService: OrderService = OrderService.getInstance()
    var paymnetData: MutableLiveData<PaymentData> = MutableLiveData()
    var netException: MutableLiveData<NetException> = MutableLiveData()
    private var dialogStatus: MutableLiveData<String> = MutableLiveData()
    var isShowAddBiling = true

    fun clearException() {
        netException.value = null
    }

    fun initMethods() {
        getIdList()
        getBillingDetails()
    }

    fun getDialogStatus(): MutableLiveData<String> {
        return dialogStatus
    }

    fun getClearDialogStatus(){
        dialogStatus.value = null
    }

    fun getOrder(): LiveData<Order> {
        return order
    }

    fun getBilling(): LiveData<BillingDetails> {
        return billing
    }

    fun getPaymentData(): MutableLiveData<PaymentData> {
        return paymnetData
    }

    fun getException(): MutableLiveData<NetException> {
        return netException
    }

    private fun getIdList() {
        val idList = mutableListOf<String>()
        for (item in cartService.getSelectedItemList()) {
            idList.add(item.id)
        }
        loadOrder(idList)
    }

    private fun loadOrder(ids: MutableList<String>) {
        order.value?.let {
            val previousOrder = order.value
            order.postValue(previousOrder)
            return
        }
        orderService.checkout(ids,
            { result ->
                order.value = result
            },
            { exception ->
                netException.value = exception
            })
    }

    private fun getBillingDetails() {
        orderService.getBillingDetails({ result ->
            billing.value = result
        }, { exception ->
            exception.printStackTrace()
        })
    }

    fun getPaymentLink() {
        getOrder().value?.let {
            PaymentService.getInstance().generatePayment(it.id,
                { result ->
                    paymnetData.postValue(result)
                }, { exception ->
                    exception.printStackTrace()
                    netException.postValue(exception)
                })
            return
        }
        dialogStatus.value = "dismiss"

    }


}
