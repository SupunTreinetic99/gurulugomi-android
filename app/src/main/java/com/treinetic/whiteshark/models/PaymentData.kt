package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Nuwan on 2/14/19.
 */
@Keep
data class PaymentData(
    @SerializedName("url")
    var url: String = "",

    @SerializedName("needToPay")
    var needToPay: Boolean = true
) {
    @SerializedName("provider")
    var provider:String = ""

    @SerializedName("paymentInfo")
    var paymentInfo:PaymentInfo = PaymentInfo()

    fun isValidUrl(): Boolean = url.isNotEmpty()
}