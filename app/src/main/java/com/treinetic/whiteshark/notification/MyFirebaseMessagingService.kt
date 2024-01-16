package com.treinetic.whiteshark.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.treinetic.whiteshark.MyApp.context
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.splash.SplashActivity
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var TAG = "MyFirebaseMS"
    private var notificationImage: String? = null
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e(TAG, "onNewToken calling ->")
        Log.d(TAG, "onNewToken : $token")
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        createNotificationChannel()
        Log.e(TAG, "onMessageReceived calling -> ${message.data}")
//        Log.d(TAG, "onMessageReceived : ${message.data.get("bookId")}")

        var gson = Gson()

        val intent = Intent(this, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        if (message.data.contains("epub")) {
            val json = message.data["epub"]
            intent.putExtra("epub", json)
            Log.d(TAG, "epub available")
            Log.d(TAG, json?:"no json")
        }
        if (message.data.contains("event")) {
            var json = message.data["event"]

            intent.putExtra("event", json)
            Log.d(TAG, "event available")
            Log.d(TAG, json?:"no Json")
        }
        if (message.data.containsKey("_notificationImage")) {
            notificationImage = message.data["_notificationImage"]

            Log.d(TAG, "notification img found $notificationImage")
        }

        val pendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(this, 2222, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getActivity(this, 2222, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        val pattern = longArrayOf(500, 500, 500, 500, 500)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, "com.treinetic.gurulugomi.dev")
            .setSmallIcon(R.drawable.notification_icon_white)
            .setContentTitle(message.data["_notificationTitle"])
            .setContentText(message.data["_notificationBody"])
            .setVibrate(pattern)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setStyle(NotificationCompat.BigTextStyle().bigText(""))
            .setContentIntent(pendingIntent) as NotificationCompat.Builder


        if (notificationImage != null) {
            Log.d(TAG, "notification image found $notificationImage")
            Handler(Looper.getMainLooper()).post(Runnable {
                Glide.with(context.applicationContext)
                    .asBitmap()
                    .load(notificationImage)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onLoadCleared(placeholder: Drawable?) {
                            Log.d(TAG, "Glide onLoadCleared")
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            Log.e(TAG, "Glide onLoadFailed")
                        }

                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {
                            Log.d(TAG, "Glide onResourceReady $resource")
                            val s =
                                NotificationCompat.BigPictureStyle().bigPicture(resource)
                            s.setSummaryText(message.data["_notificationBody"])

                            notificationBuilder.setStyle(s)
                            notificationBuilder.setLargeIcon(resource)

                            val notificationManager =
                                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            val r = Random()
                            val i1 = r.nextInt(9999)
                            notificationManager.notify(i1, notificationBuilder.build())
                        }
                    })
            })

        } else {
            Log.d(TAG, "notification image not found")
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val r = Random()
            val i1 = r.nextInt(9999)
            notificationManager.notify(i1, notificationBuilder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "com.treinetic.gurulugomi.dev"
            val description = "des"
            val importance = NotificationManager.IMPORTANCE_MAX
            @SuppressLint("WrongConstant") val channel =
                NotificationChannel("com.treinetic.gurulugomi.dev", name, importance)
            channel.description = description
            val notificationManager =
                getSystemService(
                    android.app.NotificationManager::class.java
                )
            notificationManager.createNotificationChannel(channel)
            println("channel created")
        }
    }
}