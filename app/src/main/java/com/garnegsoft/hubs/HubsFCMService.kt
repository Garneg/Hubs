package com.garnegsoft.hubs

import android.os.Looper
import android.util.Log
import android.widget.Toast

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class HubsFCMService : FirebaseMessagingService() {
	override fun onNewToken(token: String) {
		super.onNewToken(token)
	}
	
	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)
		
		Looper.prepare()
		
		Toast.makeText(this, "title: ${message.notification?.title}", Toast.LENGTH_SHORT).show()
		Log.e("fcm", "data received")
	}
}