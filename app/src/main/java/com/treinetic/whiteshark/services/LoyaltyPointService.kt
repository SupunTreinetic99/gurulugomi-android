package com.treinetic.whiteshark.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.LoyaltyPointData
import com.treinetic.whiteshark.models.Order
import com.treinetic.whiteshark.network.Net

class LoyaltyPointService {
    private val TAG = "LoyaltyPointService"

    var token: String? = null
    var loyaltyPoints: Int = 0
    var loyaltyData: LoyaltyPointData?=null

    companion object {
        var instance = LoyaltyPointService()
    }


    fun requestOtp(
        phone: String,
        orderId: String,
        success: Service.Success<String>,
        error: Service.Error
    ) {
        val body = mutableMapOf<String, Any>()
        body["phone"] = phone
        body["order_id"] = orderId
        val net = Net(
            Net.URL.LOYALTY_POINT_REQUEST_OTP, Net.NetMethod.POST,
            body, null, null, null
        )
        net.perform({ response ->
            try {
                val type = object : TypeToken<MutableMap<String, String>>() {}.type
                val data: MutableMap<String, String> = Gson().fromJson(response, type)
                token = data["token"]
                success.success(token)
            } catch (e: Exception) {
                error.error(
                    NetException(
                        "Something went wrong",
                        "json_error",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }

        }, { exception ->
            error.error(exception)
        })
    }

    fun validateOtp(
        otp: String,
        token: String? = instance.token,
        success: Service.Success<LoyaltyPointData>,
        error: Service.Error
    ) {
        val body = mutableMapOf<String, String>()
        body["otp"] = otp
        body["token"] = token!!
        val net = Net(
            Net.URL.LOYALTY_POINT_OTP_VERIFY, Net.NetMethod.POST,
            body, null, null, null
        )
        net.perform({ response ->
            try {
//                val type = object : TypeToken<MutableMap<String, Any>>() {}.type
                loyaltyData = Gson().fromJson(response, LoyaltyPointData::class.java)

                if (loyaltyData == null) {
                    error.error(
                        NetException(
                            "Loyalty points not available",
                            "LOYALTY_NOT_AVAILABLE",
                            404
                        )
                    )
                    return@perform
                }
                success.success(loyaltyData)
            } catch (e: Exception) {
                error.error(
                    NetException(
                        "Something went wrong",
                        "json_error",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }

        }, { exception ->
            error.error(exception)
        })
    }


    fun addLoyaltyPoints(
        points: Double,
        orderId: String,
        success: Service.Success<Order>,
        error: Service.Error
    ) {
        val body = mutableMapOf<String, Any>()
        body["points"] = points
        body["order_id"] = orderId
        val net = Net(
            Net.URL.LOYALTY_POINT_ADD, Net.NetMethod.POST,
            body, null, null, null
        )
        net.perform({ response ->
            try {
                val order = Gson().fromJson(response, Order::class.java)
                OrderService.getInstance().currentOrder = order
                success.success(order)
            } catch (e: Exception) {
                error.error(
                    NetException(
                        "Something went",
                        "json_error",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }

        }, { exception ->
            error.error(exception)
        })
    }

    fun removeLoyaltyPoints(
        orderId: String,
        success: Service.Success<Order>,
        error: Service.Error
    ) {
        val body = mutableMapOf<String, Any>()

        body["order_id"] = orderId
        val net = Net(
            Net.URL.LOYALTY_POINT_CANCEL, Net.NetMethod.POST,
            body, null, null, null
        )
        net.perform({ response ->
            try {
                val order = Gson().fromJson(response, Order::class.java)
                OrderService.getInstance().currentOrder = order
                success.success(order)
            } catch (e: Exception) {
                error.error(
                    NetException(
                        "Something went",
                        "json_error",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }

        }, { exception ->
            error.error(exception)
        })
    }


}