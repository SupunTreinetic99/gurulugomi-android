package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class LoyaltyPoints {
    @SerializedName("loyalty_points_discount")
    var loyaltyPointsDiscount: Double = 0.00

    @SerializedName("loyalty_points_amount")
    var loyaltyPointsAmount: Double = 0.00

    @SerializedName("value_of_a_point")
    var value_of_a_point: Double = 0.00
}