package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class User(
    @SerializedName("id")
    var user_id: String,
    @SerializedName("name")
    var name: String,
    @SerializedName("email")
    var email: String,
    @SerializedName("gender")
    var gender: String? = null,
    @SerializedName("image")
    var image: Image? = null,
    @SerializedName("contact_number")
    var contact_number: String? = null,
    @SerializedName("provider")
    var provider: String? = null,
    @SerializedName("status")
    var status: String? = null,
    var password: String
) {
    @SerializedName("country_code")
    var country_code: String? = null


    fun getFullContactNumber(): String {
        return "$country_code $contact_number"
    }
}