package com.treinetic.whiteshark.notification

import android.content.Intent
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Event
import com.treinetic.whiteshark.models.Promotion
import java.lang.Exception

class NotificationManager private constructor() {


    private var DEFAULT_TOPIC = "Books"
    private val TAG = "NotificationManager"
    private var gson = Gson()
    var event: Event? = null
    var book: Book? = null
    var promotion: Promotion? = null
    var isUsedNotificationData = true

    companion object {
        val instance = NotificationManager()

    }

    fun subscribeTo(topic: String = DEFAULT_TOPIC) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Subscribe toTopic success"
                if (!task.isSuccessful) {
                    msg = "Failed to subscribe to topic"
                }
                Log.d(TAG, msg)
            }
        generateToken()
    }

    fun unSubscribeFrom(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "unSubscribe toTopic success"
                if (!task.isSuccessful) {
                    msg = "Failed to unSubscribe to topic"
                }
                Log.d(TAG, msg)
            }
    }

    fun generateToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener {task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "getInstanceId failed", task.exception)
                return@OnCompleteListener
            }

            // Get new Instance ID token
            val token = task.result

            // Log and toast
            val msg = "TOKEN :$token"
            Log.d(TAG, msg)

        })
    }


    fun getNotificationData(intent: Intent) {
        if (intent.extras == null) return

        try {
            if (intent.extras!!.containsKey("epub")) {
                val epubJson = intent.getStringExtra("epub")
                Log.d(TAG, "epub json \n $epubJson")
                book = gson.fromJson(epubJson, Book::class.java)
                book?.isFill = false
                event = null
                promotion = null
                isUsedNotificationData = false

                return
            }

            if (intent.extras!!.containsKey("event")) {
                var eventJson = intent.getStringExtra("event")
                eventJson?.let {
                    eventJson = eventJson?.replace("\"custom_data\":\"\"", "\"custom_data\": null")
                }
                Log.d(TAG, "event json \n $eventJson")
                event = gson.fromJson(eventJson, Event::class.java)
                book = null
                promotion = null
                isUsedNotificationData = false
                return
            }

            if (intent.extras!!.containsKey("promotion")) {
                var promotionJson = intent.getStringExtra("promotion")
                Log.d(TAG, "promotion json \n $promotionJson")
                promotion = gson.fromJson(promotionJson, Promotion::class.java)
                event = null
                book = null
                isUsedNotificationData = false
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun hasNotificationData(): Boolean {
        return event != null || book != null || promotion!=null
    }

    fun clear() {
        event = null
        book = null
        book?.isFill  = false
        isUsedNotificationData = true
    }


}