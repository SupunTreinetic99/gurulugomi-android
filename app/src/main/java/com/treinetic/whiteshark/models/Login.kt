package com.treinetic.whiteshark.models

import android.util.Log
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.treinetic.whiteshark.notification.NotificationManager

@Keep
data class Login(
    @SerializedName("user")
    var user: User,
    @SerializedName("billingDetails")
    var billingDetails: BillingDetails? = null,
    @SerializedName("category")
    var category: MutableList<Category>
) {
    @SerializedName("topics")
    var topics: ArrayList<NotificationTopic> = arrayListOf()

    @SerializedName("services")
    var services: Services = Services()

//    @SerializedName("myLibrary")
//    var myLibrary: ArrayList<Books> = arrayListOf()
//
//    @SerializedName("fonts")
//    var fonts: ArrayList<Font> = arrayListOf()
//    @SerializedName("home")
//    var home: HomeData? = null


    fun subscribeForNotificationTopics() {

        topics?.forEach {
            Log.d("Login", "topic : ${it.topic}_android  subscribe : ${it.subscribe}")
            NotificationManager.instance.unSubscribeFrom(it.topic)
            if (it.subscribe) {
                NotificationManager.instance.subscribeTo(it.topic + "_android")
            } else {
                NotificationManager.instance.unSubscribeFrom(it.topic + "_android")
            }

        }

    }
}




