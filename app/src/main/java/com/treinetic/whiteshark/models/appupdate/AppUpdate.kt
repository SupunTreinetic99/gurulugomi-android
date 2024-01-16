package com.treinetic.whiteshark.models.appupdate

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.util.HashMap

/**
 * Created by Nuwan on 8/31/18.
 */
@Keep
class AppUpdate {
    @SerializedName("update")
    var update: Update? = null
    @SerializedName("configuration")
    var configuration: Map<String, Any> = HashMap()


    fun hasCriticalUpdate(): Boolean {
        update?.let {
            return it.critical!=null && it.critical!! && it.available
        }
        return false
    }

    fun hasUpdate(): Boolean {
        update?.let {
            return !(it.critical!=null && it.critical!!) && it.available
        }
        return false
    }

    fun getMessage(): String {
        update?.let {
            return it.message ?: ""
        }
        return ""
    }


}
