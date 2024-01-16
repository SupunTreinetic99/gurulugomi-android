package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class Author(
    @SerializedName("user_id") var
    user_id: String,
    @SerializedName("reference_no")
    var reference_no: String? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("user")
    var user: User
)