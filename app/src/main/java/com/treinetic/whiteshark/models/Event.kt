package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.treinetic.whiteshark.util.DateFormatUtil
import kotlin.collections.ArrayList
@Keep
data class Event(

    @SerializedName("id")
    val id: String?,

    @SerializedName("event")
    var event: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("link")
    val link: String?,

    @SerializedName("contact_number")
    val contactNumber: String? = null,

    @SerializedName("event_date")
    val eventDate: String?,


    @SerializedName("start_time")
    val startTime: String?,


    @SerializedName("end_time")
    val endTime: String?,

    @SerializedName("custom_data")
    val customData: ArrayList<CustomData>? = ArrayList(),

    @SerializedName("event_images")
    var images: List<BookImages>? = null,

    @SerializedName("event_end_date")
    var endDate: String? = null


) {

    fun getFormattedDate(date: String): FormattedDate {
        return DateFormatUtil.getInstance().getFormattedDate(date)
    }


}