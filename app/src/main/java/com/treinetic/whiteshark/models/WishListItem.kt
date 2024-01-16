package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Nuwan on 2/11/19.
 */
@Keep
data class WishListItem(
    @SerializedName("id")
    var id: String,
    @SerializedName("book_id")
    var book_id: String,
    @SerializedName("user_customer_user_id")
    var customerId: String,
    @SerializedName("book")
    var book: Book
)