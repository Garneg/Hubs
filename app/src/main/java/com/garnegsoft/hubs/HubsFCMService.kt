package com.garnegsoft.hubs

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.core.os.toPersistableBundle

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class HubsFCMService : FirebaseMessagingService() {
	override fun onNewToken(token: String) {
		super.onNewToken(token)
	}
	
	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)
		
		Looper.prepare()
		
		if (Build.VERSION.SDK_INT >= 26) {
			val channel = NotificationChannel("updateNotifications", "Обновления", NotificationManager.IMPORTANCE_HIGH)
			
			val notificationManager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
			notificationManager.createNotificationChannel(channel)
			Log.e("fcm", "update notification channel registered!")
			
			message.notification?.let {
				val notification = NotificationCompat.Builder(this, it.channelId ?: "updateNotifications")
					.setExtras(Bundle().apply { message.data.forEach { key, value -> putString(key, value) } })
					.setContentTitle(it.title)
					.setContentText(it.body)
					.setSmallIcon(R.drawable.notification_default_icon)
					.build()
				notificationManager.notify(0, notification)
				
			}
			
		}
		
		
		//Toast.makeText(this, "title: ${message.notification?.title}", Toast.LENGTH_SHORT).show()
		Log.e("fcm", "data received")
	}
}