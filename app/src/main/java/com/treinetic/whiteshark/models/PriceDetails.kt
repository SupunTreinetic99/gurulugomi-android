package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class PriceDetails(
    @SerializedName("originalPrice")
    var originalPrice: Double,
    @SerializedName("currency")
    var currency: String,
    @SerializedName("discount")
    var discount: Double,
    @SerializedName("visiblePrice")
    var visiblePrice: Double,
    @SerializedName("printed_price")
    var printedPrice: Double,
    @SerializedName("isFree")
    var isFree: Boolean = false,
    @SerializedName("isOffer")
    var isOffer: Boolean = false
) {





}