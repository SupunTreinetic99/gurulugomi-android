package com.treinetic.whiteshark.services

import com.google.gson.Gson
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.*
import com.treinetic.whiteshark.network.Net


class OrderService {

    private val TAG = "OrderService"
    var billingDetails: BillingDetails? = null
    private var purchased: Purchased? = null

    var currentOrder: Order? = null

    companion object {
        private val instance = OrderService()


        fun getInstance(): OrderService {
            return instance
        }
    }

    fun clearOrder() {
        currentOrder = null
    }

    fun checkout(ids: MutableList<String>, success: Service.Success<Order>, error: Service.Error) {
        val body = mutableMapOf<String, MutableList<String>>()
        body["cartItemIds"] = ids
        val net = Net(
            Net.URL.CHECKOUT, Net.NetMethod.POST,
            body, null, null, null
        )
        net.perform({ response ->
            try {
                val order = Gson().fromJson(response, Order::class.java)
                currentOrder = order
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

    fun getBillingDetails(success: Service.Success<BillingDetails>, error: Service.Error) {

        if (billingDetails != null) {
            success.success(billingDetails)
            return
        }
        val net = Net(
            Net.URL.BILLING_DETAILS,
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )

        net.perform({ response ->
            val updatedBilling =
                Gson().fromJson<BillingDetails>(response, BillingDetails::class.java)
            billingDetails = updatedBilling
            success.success(billingDetails)
        }, { exception ->
            error.error(exception)
        })

    }


//    fun hasBillingDetails(): Boolean = billingDetails == null

    fun addBillingDetails(
        bodyData: BillingDetails,
        success: Service.Success<BillingDetails>,
        error: Service.Error
    ) {

        val body = mutableMapOf<String, String>()
        body["firstName"] = bodyData.firstName
        bodyData.lastName.let {
            body["lastName"] = it
        }
        body["email"] = bodyData.email
        body["contactNumber"] = bodyData.contact
//        body["address1"] = bodyData.address1
//        body["addressCity"] = bodyData.addressCity
//        body["addressCountry"] = bodyData.country
//        body["address2"] = bodyData.address2
//        body["zipCode"] = bodyData.zipCode
//        body["countryCode"] = bodyData.countryCode


        val net = Net(
            Net.URL.ADD_BILLING_DETAILS,
            Net.NetMethod.POST,
            body,
            null,
            null,
            null
        )
        net.perform({ response ->

            try {
                val updatedData =
                    Gson().fromJson<BillingDetails>(response, BillingDetails::class.java)
                updatedData.contact = bodyData.contact
                billingDetails = updatedData
                success.success(updatedData)
            } catch (e: Exception) {
                NetException(
                    "something went wrong",
                    "json error",
                    ErrorCodes.JSON_ERROR
                )
            }

        }, { exception ->
            error.error(exception)
        })

    }

    fun getPurchasedHistory(
        success: Service.Success<Purchased>,
        error: Service.Error,
        requireUpdate: Boolean = false
    ) {

        if (purchased != null && !requireUpdate) {
            success.success(purchased)
            return
        }

        val net = Net(
            Net.URL.PURCHASED_HISTORY,
            Net.NetMethod.GET, null, null, null, null
        )

        net.perform({ response ->
            try {
                val updatePurchased = Gson().fromJson<Purchased>(response, Purchased::class.java)
                purchased = updatePurchased
                success.success(purchased)
            } catch (e: Exception) {
                NetException(
                    "Something went wrong",
                    "Josn pass error",
                    ErrorCodes.JSON_ERROR
                )
            }

        }, { exception ->
            error.error(exception)
        })

    }

    fun getOrderDetails(
        orderId: String,
        success: Service.Success<Order>,
        error: Service.Error
    ) {

        var pathParam: MutableMap<String, Any> = mutableMapOf()
        pathParam["id"] = orderId
        val net = Net(
            Net.URL.ORDER,
            Net.NetMethod.GET,
            null,
            null,
            pathParam,
            null
        )
        net.perform({ response ->

            try {
                val order =
                    Gson().fromJson<Order>(response, Order::class.java)
                success.success(order)
            } catch (e: Exception) {
                NetException(
                    "something went wrong",
                    "json error",
                    ErrorCodes.JSON_ERROR
                )
            }

        }, { exception ->
            error.error(exception)
        })


    }

    fun fillBillingDetails(loginData: Login) {

        billingDetails = loginData.billingDetails ?: return
        billingDetails?.contact = loginData.user.contact_number ?: ""
        billingDetails?.countryCode = loginData.user.country_code ?: ""

    }

//    fun isOrderSame(orderItems: ArrayList<OrderItem>, cartItems: ArrayList<CartItem>) {
//
//
//    }


    fun clear() {
        billingDetails = null
        purchased = null
    }


    fun addPromoCode(
        promoCode: String,
        orderId: String,
        success: Service.Success<Order>,
        error: Service.Error
    ) {

        val body = mutableMapOf<String, String>()
        body["code"] = promoCode
        body["order_id"] = orderId
        val net = Net(
            Net.URL.ADD_PROMO_CODE, Net.NetMethod.POST,
            body, null, null, null
        )
        net.perform({ response ->
            try {
                val order = Gson().fromJson(response, Order::class.java)
                currentOrder = order
                success.success(order)
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

    fun removePromoCode(
        promoCode: String,
        orderId: String,
        success: Service.Success<Order>,
        error: Service.Error
    ) {
        val body = mutableMapOf<String, String>()
        body["code"] = promoCode
        body["order_id"] = orderId
        val net = Net(
            Net.URL.REMOVE_PROMO_CODE, Net.NetMethod.POST,
            body, null, null, null
        )
        net.perform({ response ->
            try {
                val order = Gson().fromJson(response, Order::class.java)
                currentOrder = order
                success.success(order)
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


    fun applyEVoucher(
        code: String,
        orderId: String,
        success: Service.Success<Order>,
        error: Service.Error
    ) {
        val body = mutableMapOf<String, String>()
        body["e_voucher"] = code
        body["order_id"] = orderId
        val net = Net(
            Net.URL.E_VOUCHER_ADD, Net.NetMethod.POST,
            body, null, null, null
        )
        net.perform({ response ->
            try {
                val order = Gson().fromJson(response, Order::class.java)
                currentOrder = order
                success.success(order)
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

    fun removeEVoucher(
        code: String,
        orderId: String,
        success: Service.Success<Order>,
        error: Service.Error
    ) {
        val body = mutableMapOf<String, String>()
        body["e_voucher"] = code
        body["order_id"] = orderId
        val net = Net(
            Net.URL.E_VOUCHER_CANCEL, Net.NetMethod.POST,
            body, null, null, null
        )
        net.perform({ response ->
            try {
                val order = Gson().fromJson(response, Order::class.java)
                currentOrder = order
                success.success(order)
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

}
