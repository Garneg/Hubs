package com.garnegsoft.hubs

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
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
				val intent = Intent(applicationContext, MainActivity::class.java).apply {
					flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
					putExtras(Bundle().apply { message.data.forEach { key, value -> putString(key, value) } })
				}
				val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
				
				val notification = NotificationCompat.Builder(this, it.channelId ?: "updateNotifications")
					.setContentTitle(it.title)
					.setContentText(it.body)
					.setSmallIcon(R.drawable.notification_default_icon)
					.setContentIntent(pendingIntent)
					.setAutoCancel(true)
					.addExtras(Bundle().apply { message.data.forEach { key, value -> putString(key, value) } })
					.build()
				notificationManager.notify(0, notification)
				
			}
			
		}
		
		
		//Toast.makeText(this, "title: ${message.notification?.title}", Toast.LENGTH_SHORT).show()
		Log.e("fcm", "data received")
	}
}