package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Publisher (
    @SerializedName("id")
    val id: String?,

    @SerializedName("publisherID")
    val publisherID: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("publishingType")
    val publishingType: Any?,

    @SerializedName("image")
    val image: Image?,

    @SerializedName("addressNumber")
    val addressNumber: String?,

    @SerializedName("street")
    val street: String?,

    @SerializedName("city")
    val city: String?,

    @SerializedName("contactPerson")
    val contactPerson: Any?,

    @SerializedName("contactNumber")
    val contactNumber: String?,

    @SerializedName("email")
    val email: Any?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("bankName")
    val bankName: Any?,

    @SerializedName("accountNumber")
    val accountNumber: Any?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("status")
    val status: String?

)