package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Nuwan on 2/7/19.
 */
@Keep
data class CustomData(
    @SerializedName("key")
    var key: String,
    @SerializedName("value")
    var value: String

) {
}