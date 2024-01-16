package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
class EVoucher {

    @SerializedName("e_voucher_amount")
    var eVoucherAmount: Double = 0.00

    @SerializedName("e_voucher")
    var e_voucher: String? = null
}