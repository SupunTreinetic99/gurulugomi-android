package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class Message(
    @SerializedName("message_id")
    var messageId: String,
    @SerializedName("message")
    var message: String
)