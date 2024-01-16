package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class Purchased(
    @SerializedName("current_page")
    var currentPage: Int,
    @SerializedName("data")
    var orderList: MutableList<Order>,
    @SerializedName("first_page_url")
    var firstPageUrl: String,
    @SerializedName("from")
    var from: String,
    @SerializedName("last_page")
    var lastPage: Int,
    @SerializedName("last_page_url")
    var lastPageUrl: String,
    @SerializedName("next_page_url")
    var nextPageUrl: String,
    @SerializedName("path")
    var path: String,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("prev_page_url")
    var prevPageUrl: String,
    @SerializedName("to")
    var to: Int,
    @SerializedName("total")
    var total: Int
)