package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.util.*
@Keep
data class BookCategory(

    @SerializedName("current_page")
    var current_page: Int? = 0,

    @SerializedName("data")
    var data: ArrayList<Category> = arrayListOf(),

    @SerializedName("first_page_url")
    var first_page_url: String? = null,

    @SerializedName("from")
    var from: Int = 0,

    @SerializedName("last_page")
    var last_page: Int = 0,

    @SerializedName("last_page_url")
    var last_page_url: String? = null,

    @SerializedName("next_page_url")
    var next_page_url: String? = null,

    @SerializedName("path")
    var path: String? = null,

    @SerializedName("per_page")
    var per_page: Int = 0,

    @SerializedName("prev_page_url")
    var prev_page_url: String? = null,

    @SerializedName("to")
    var to: Int = 0,

    @SerializedName("total")
    var total: Int = 0

){
    @SerializedName("books_count")
    var booksCount: Int = 0
}