package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class PaymentInfo() {
    @SerializedName("address")
    var address: String = ""

    @SerializedName("amount")
    var amount: Double = 0.00

    @SerializedName("cancel_url")
    var cancelUrl: String = ""

    @SerializedName("city")
    var city: String = ""

    @SerializedName("country")
    var country: String = "Sri Lanka"

    @SerializedName("currency")
    var currency: String = "LKR"

    @SerializedName("email")
    var email: String? = null

    @SerializedName("first_name")
    var firstName: String = ""

    @SerializedName("items")
    var items: String = ""

    @SerializedName("last_name")
    var lastName: String? = ""

    @SerializedName("merchant_id")
    var merchantId: String = ""

    @SerializedName("notify_url")
    var notifyUrl: String = ""

    @SerializedName("order_id")
    var orderId: Int = -1

    @SerializedName("phone")
    var phone: String? = null

    @SerializedName("return_url")
    var returnUrl: String = ""


    fun isAmountValidForPayHerePayment()= !(amount > 0.00 && amount < 50.00)

}