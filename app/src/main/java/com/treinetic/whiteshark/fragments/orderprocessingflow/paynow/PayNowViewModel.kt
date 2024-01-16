package com.treinetic.whiteshark.fragments.orderprocessingflow.paynow

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.*
import com.treinetic.whiteshark.services.*
import com.treinetic.whiteshark.util.extentions.toCurrency
import com.treinetic.whiteshark.viewmodels.BaseViewModel

class PayNowViewModel : BaseViewModel() {
    var TAG = "PayNowViewModel"
    var totalPrice: MutableLiveData<String> = MutableLiveData()
    var promoCode: MutableLiveData<String> = MutableLiveData()
    var loyaltyPoints: MutableLiveData<String> = MutableLiveData()
    var discount: MutableLiveData<String> = MutableLiveData()
    var totalBeforeDiscount: MutableLiveData<String> = MutableLiveData()
    var promoCodeLabel: MutableLiveData<String> = MutableLiveData()
    var promotionCodeAmount: MutableLiveData<String> = MutableLiveData()
    var isPaymentInProgress = false
    var eVoucherAmount: MutableLiveData<String> = MutableLiveData()


    var paymentData: MutableLiveData<PaymentData> = MutableLiveData()
    var paymentException: MutableLiveData<NetException> = MutableLiveData()

    var promoCodeClear: MutableLiveData<Order> = MutableLiveData()
    var loyaltyPointsClear: MutableLiveData<Order> = MutableLiveData()
    var eVoucherClear: MutableLiveData<Order> = MutableLiveData()

    var orderSuccess: MutableLiveData<String> = MutableLiveData()
    var orderError: MutableLiveData<String> = MutableLiveData()

    var myLibrarySuccess=false
    var myLibraryError=false

    var cartSuccess=false
    var cartError=false

    var purchaseHistorySuccess=false
    var purchaseHistoryError=false

    init {
        totalPrice.value = "Rs. 0.00"
        promoCode.value = "Rs. 0.00"
        loyaltyPoints.value = ""
        totalBeforeDiscount.value = "Rs. 0.00"
        promotionCodeAmount.value = ""
        eVoucherAmount.value = ""
        promoCodeLabel.value = "Promo Code"
    }

    fun setData() {

        OrderService.getInstance().currentOrder?.totalDiscount?.let { it ->
            discount.value = it.toCurrency("Rs.")
        }

        OrderService.getInstance().currentOrder?.totalPaid?.let {
            totalPrice.value = it.toCurrency("Rs.")
        }

        OrderService.getInstance().currentOrder?.totalAmount?.let {
            totalBeforeDiscount.value = it.toCurrency("Rs.")
        }

        OrderService.getInstance().currentOrder?.totalDiscount?.let {
            discount.value = "(${it.toCurrency("Rs.")})"
        }

        OrderService.getInstance().currentOrder?.loyaltyPoints?.loyaltyPointsAmount?.let {
            if (it == 0.00) {
                loyaltyPoints.value = ""
                return@let
            }
            loyaltyPoints.value = it.toCurrency("Rs.")
        }

        OrderService.getInstance().currentOrder?.eVoucher?.eVoucherAmount?.let {
            if (it == 0.00) {
                eVoucherAmount.value = ""
                return@let
            }

            eVoucherAmount.value = it.toCurrency("Rs.")
        }

        OrderService.getInstance().currentOrder?.promotionCode?.let { promotionCode ->
            promotionCode.promotionCodeAmount?.let {
                if (it == 0.00) {
                    promotionCodeAmount.value = ""
                    return@let
                }
                promotionCodeAmount.value = it.toCurrency("Rs.")
            }
            promotionCode?.promotionCode?.let {
                promoCodeLabel.value = "$it"
            }

        }


    }


    fun getPaymentLink() {
        isPaymentInProgress = true
        OrderService.getInstance().currentOrder?.let {
            PaymentService.getInstance().generatePayment(it.id,
                { result ->
                    isPaymentInProgress = false
                    paymentData.postValue(result)
                }, { exception ->
                    isPaymentInProgress = false
                    exception.printStackTrace()
                    paymentException.postValue(exception)
                })
            return
        }
    }


    fun clearEvoucher() {
        isPaymentInProgress = true
        val code = OrderService.getInstance().currentOrder?.eVoucher?.e_voucher
        val orderId = OrderService.getInstance().currentOrder?.id
        OrderService.getInstance().removeEVoucher(
            code = code!!,
            orderId = orderId!!,
            success = {
                isPaymentInProgress = false
                eVoucherClear.postValue(it)
            },
            error = { exception ->
                isPaymentInProgress = false
                exception.printStackTrace()
                paymentException.postValue(exception)
            }
        )
    }

    fun clearLoyaltyPoints() {
        isPaymentInProgress = true
        val orderId = OrderService.getInstance().currentOrder?.id
        LoyaltyPointService.instance.removeLoyaltyPoints(
            orderId = orderId!!,
            success = {
                isPaymentInProgress = false
                loyaltyPointsClear.postValue(it)
            },
            error = { exception ->
                isPaymentInProgress = false
                exception.printStackTrace()
                paymentException.postValue(exception)
            }
        )
    }

    fun clearPromoCode() {
        isPaymentInProgress = true
        val code = OrderService.getInstance().currentOrder?.promotionCode?.promotionCode
        val orderId = OrderService.getInstance().currentOrder?.id
        OrderService.getInstance().removePromoCode(
            promoCode = code!!,
            orderId = orderId!!,
            success = {
                isPaymentInProgress = false
                promoCodeClear.postValue(it)
            },
            error = { exception ->
                isPaymentInProgress = false
                exception.printStackTrace()
                paymentException.postValue(exception)
            }
        )
    }


     fun updateMylibrary() {
        BookService.getInstance().fetchMyLibrary({ result: Books? ->
            updateCart()
            Log.d(TAG, "success fetch my library")
        }, { exception: NetException ->
            exception.printStackTrace()
            Log.e(TAG, "Error in fetch my library")
            updateCart()
        })
    }

     fun updateCart() {
        CartService.getInstance()
            .loadCartRequest({ result: List<CartItem?>? ->
                Log.d(TAG, "success fetch cart")
                updatePurchaseHistory()
            }, { exception: NetException ->
                exception.printStackTrace()
                Log.e(TAG, "Error in fetch cart")
                updatePurchaseHistory()
            }, true)
    }

     fun updatePurchaseHistory() {
        OrderService.getInstance().getPurchasedHistory(
            { result: Purchased? ->
                Log.d(TAG, "success update Purchase History")
                orderSuccess.postValue("success")
            },
            { exception: NetException? ->
                Log.e(TAG, "Error in update Purchase History")
                orderError.postValue("success")
            }, true
        )
    }



}