package com.treinetic.whiteshark.services

import android.util.Log
import com.google.gson.Gson
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.PaymentData
import com.treinetic.whiteshark.network.Net

/**
 * Created by Nuwan on 2/14/19.
 */
class PaymentService {

    private val TAG = "PaymentService"
    var paymentData: PaymentData? = null

    companion object {
        private val instance = PaymentService()
        fun getInstance(): PaymentService =
            instance
    }

    fun generatePayment(orderId: String, success: Service.Success<PaymentData>, error: Service.Error) {

        val body = mutableMapOf<String, String>()
        body.put("orderId", orderId)

        val net = Net(
            Net.URL.PAYMENT_GENERATE,
            Net.NetMethod.POST,
            body,
            null,
            null,
            null
        )
        net.perform({ response ->
            run {
                Log.d(TAG, "Success generatePayment")
                try {
                    paymentData = Gson().fromJson(response, PaymentData::class.java)

                    success.success(paymentData)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    error.error(
                        NetException(
                            "Something went",
                            "json_error",
                            ErrorCodes.JSON_ERROR
                        )
                    )
                }
            }
        }, { exception ->
            error.error(exception)
            Log.e(TAG, exception.toString())
        })

    }

}