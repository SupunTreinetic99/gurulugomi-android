package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class Promotion(
    @SerializedName("id")
    var id: String,
    @SerializedName("promotion")
    var promotion: String,
    @SerializedName("image")
    var image: Image,
    @SerializedName("timely_discount")
    var timely_discount: TimelyDiscount? = null,
    @SerializedName("video")
    var videoLink: String? = null
)