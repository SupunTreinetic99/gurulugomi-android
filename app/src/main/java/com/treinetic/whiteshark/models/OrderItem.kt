package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class OrderItem(
    @SerializedName("id")
    var id: String,
    @SerializedName("order_id")
    var orderId: String,
    @SerializedName("book_id")
    var bookId: String,
    @SerializedName("amount")
    var amount: Double,
    @SerializedName("discount")
    var discount: Double,
    @SerializedName("paid")
    var paid: Double,
    @SerializedName("book")
    var book: Book
) {

    @SerializedName("promotions")
    var promotions: ArrayList<Promotion> = arrayListOf()
}