package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class Rating(
    @SerializedName("order_item_id")
    var orderItemId: String,
    @SerializedName("book_id")
    var bookId: String,
    @SerializedName("user_customer_user_id")
    var userCustomerUserId: String,
    @SerializedName("rating")
    var rating: Float,
    @SerializedName("review")
    var review: String,
    @SerializedName("user_customer")
    var userCustomer: Customer? = null,
    @SerializedName("created_at")
    var createdAt: String = "",
    @SerializedName("isMyReview")
    var isMyReview: Boolean = false
) {
    fun getIsMyReview(): Boolean {
        return isMyReview
    }
}