package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class Cart(
    @SerializedName("cartItem")
    var cartItem: MutableList<CartItem>? = null
)