package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class BillingDetails(
    @SerializedName("user_customer_user_id")
    var userId: String,
    @SerializedName("first_name")
    var firstName: String,
    @SerializedName("last_name")
    var lastName: String,
    @SerializedName("email")
    var email: String,
    @SerializedName("contact_number")
    var contact: String
//    @SerializedName("address_line_1")
//    var address1: String,
//    @SerializedName("city")
//    var addressCity: String,
//    @SerializedName("country")
//    var country: String,
//    @SerializedName("address_line_2")
//    var address2: String,
//    @SerializedName("zip_code")
//    var zipCode: String

) {
    @SerializedName("countryCode")
    var countryCode: String = ""

    fun isFilled() = !userId.isNullOrEmpty()
            && !firstName.isNullOrEmpty()
            && !lastName.isNullOrEmpty()
            && !email.isNullOrEmpty()
//            && !address1.isNullOrEmpty()
//            && !addressCity.isNullOrEmpty()
//            && !country.isNullOrEmpty()


}
