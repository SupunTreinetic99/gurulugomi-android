package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ShareUrl (
    @SerializedName("url")
    var url: String?
)