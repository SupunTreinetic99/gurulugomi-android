package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class Image(
    @SerializedName("small")
    var small: String? = null,
    @SerializedName("medium")
    var medium: String? = null,
    @SerializedName("large")
    var large: String? = null,
    @SerializedName("ex_large")
    var extraLarge: String? = null


) {

    fun getSmallImage(): String? {
        if (small != null) return small
        if (medium != null) return medium
        if (large != null) return large
        return small
    }

    fun getImage(): String? {
        if (medium != null) return medium
        if (small != null) return small
        if (large != null) return large
        return small
    }

    fun getLargeImage(): String? {
        if (large != null) return large
        if (medium != null) return medium
        if (small != null) return small
        return small
    }

    fun getExtraLargeImage(): String? {
        if (extraLarge != null) return extraLarge
        if (large != null) return large
        if (medium != null) return medium
        if (small != null) return small
        return small
    }


}