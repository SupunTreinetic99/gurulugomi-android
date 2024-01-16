package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class Translator (
    @SerializedName("id")
    var id: String,
    @SerializedName("name")
    var name: String
)