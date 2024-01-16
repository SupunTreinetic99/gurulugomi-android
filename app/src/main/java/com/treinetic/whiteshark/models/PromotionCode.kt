package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
class PromotionCode {
    @SerializedName("promotion_code_discount")
    var promotionCodeDiscount: Double = 0.00

    @SerializedName("promotion_code_amount")
    var promotionCodeAmount: Double = 0.00

    @SerializedName("promotion_code")
    var promotionCode: String? = null

}