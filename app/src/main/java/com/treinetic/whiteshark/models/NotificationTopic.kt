package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
class NotificationTopic {
    @SerializedName("topic")
    var topic: String = ""
    @SerializedName("subscribe")
    var subscribe: Boolean = false
}
