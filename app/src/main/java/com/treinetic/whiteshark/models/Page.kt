package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class Page(

    @SerializedName("category")
    var category: String? = null,
    @SerializedName("books")
    var books: Books? = null


)