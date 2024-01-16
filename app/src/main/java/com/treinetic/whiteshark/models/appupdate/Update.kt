package com.treinetic.whiteshark.models.appupdate

import androidx.annotation.Keep
import androidx.room.Ignore
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.HashMap

/**
 * Created by Nuwan on 8/31/18.
 */
@Keep
class Update {
    @SerializedName("message")
    var message: String? = null

    @SerializedName("critical")
    var critical: Boolean? = false

    @SerializedName("available")
    var available: Boolean = false

    @SerializedName("extras")
    var extras: Map<String, Any>? = HashMap()

    companion object {
        @Expose(serialize = false)
        const val STORE_LINK = "store_link"

        @Expose(serialize = false)
        const val HOME_MESSAGE_TITTLE = "home_message_title"

        @Expose(serialize = false)
        const val HOME_MESSAGE_BODY = "home_message_body"

        @Expose(serialize = false)
        const val IS_HOME_MESSAGE_DISPLAY = "is_home_message_display"

        @Expose(serialize = false)
        const val E_VOUCHER_LINK = "evoucher_link"
    }

}


