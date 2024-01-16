package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class LoyaltyPointData {
    @SerializedName("points")
    var points: Double = 0.00

    @SerializedName("value")
    var value: Double = 0.00
}