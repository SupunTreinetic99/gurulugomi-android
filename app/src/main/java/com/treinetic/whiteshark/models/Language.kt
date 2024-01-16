package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class Language(
    @SerializedName("id")
    var id: String,
    @SerializedName("code")
    var code: String? = null,
    @SerializedName("language")
    var language: String? = null
)