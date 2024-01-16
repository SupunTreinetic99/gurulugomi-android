package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class Font(
    @SerializedName("id")
    var id: String,
    @SerializedName("")
    var name: String,
    @SerializedName("font_url")
    var fontUrl: String
)