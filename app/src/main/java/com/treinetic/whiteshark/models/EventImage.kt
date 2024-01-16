package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Nuwan on 2/6/19.
 */
@Keep
data class EventImage(
    @SerializedName("id")
    var id: String? = null,

    @SerializedName("event_id")
    var eventId: String? = null,

    @SerializedName("image")
    var image: Image? = null



) {
}