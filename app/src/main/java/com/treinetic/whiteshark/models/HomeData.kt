package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class HomeData(

    @SerializedName("offers")
    var offers: Page,
    @SerializedName("free")
    var free: Page,
    @SerializedName("categories")
    var categories: BookCategory,
    @SerializedName("newArrivals")
    var newArrivals: Page,
    @SerializedName("bestSeller")
    var bestSeller: Page,
    @SerializedName("unpublishedEpubs")
    var unpublishedEpubs: Page? = null

)