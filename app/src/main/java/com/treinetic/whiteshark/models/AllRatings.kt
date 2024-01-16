package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class AllRatings(
    @SerializedName("current_page")
    var currentPage: Int,

    @SerializedName("data")
    var data: ArrayList<Rating>,

    @SerializedName("first_page_url")
    var firstPageUrl: String,

    @SerializedName("from")
    var from: Int,

    @SerializedName("last_page")
    var lastPage: Int,

    @SerializedName("last_page_url")
    var lastPageUrl: String,

    @SerializedName("next_page_url")
    var nextPageUrl: String? = null,

    @SerializedName("path")
    var path: String,

    @SerializedName("per_page")
    var perPage: Int,

    @SerializedName("prev_page_url")
    var prevPageUrl: String? = null,

    @SerializedName("to")
    var to: Int,

    @SerializedName("total")
    var total: Int
)