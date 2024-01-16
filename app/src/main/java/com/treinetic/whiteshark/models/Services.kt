package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
class Services {
    @SerializedName("loyalty_point")
    var loyaltyPoints: Boolean = false

    @SerializedName("e_voucher")
    var promoCode: Boolean = false

    @SerializedName("promo_code")
    var eVoucher: Boolean = false

}