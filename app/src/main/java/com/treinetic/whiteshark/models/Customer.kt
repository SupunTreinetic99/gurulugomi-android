package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class Customer(
    @SerializedName("user_id")
    var userId: String,
    @SerializedName("reference_no")
    var referenceNo: String,
    @SerializedName("user")
    var user: User

)