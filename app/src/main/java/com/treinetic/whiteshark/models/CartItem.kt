package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class CartItem(
    @SerializedName("id")
    var id: String,
    @SerializedName("book_id")
    var bookId: String,
    @SerializedName("user_customer_user_id")
    var userId: String? = null,
    @SerializedName("status")
    var status: String? = null,
    @SerializedName("book")
    var book: Book

)