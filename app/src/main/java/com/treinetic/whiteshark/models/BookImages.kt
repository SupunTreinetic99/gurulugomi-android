package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class BookImages(
    @SerializedName("book_id")
    var book_id: String,
    @SerializedName("image")
    var image: Image,
    @SerializedName("type")
    var type: String? = null,
    @SerializedName("size")
    var size: Double

)