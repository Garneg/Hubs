package com.garnegsoft.hubs.api.me

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.utils.placeholderAvatarUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.Calendar

class MeDataUpdateWorker(
	appContext: Context,
	params: WorkerParameters
) : CoroutineWorker(appContext, params) {
	override suspend fun doWork(): Result {
		if (Build.VERSION.SDK_INT >= 26) {
			
			val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			val channel = NotificationChannel("updateMe", "Обновление данных авторизации", NotificationManager.IMPORTANCE_DEFAULT)
			notificationManager.createNotificationChannel(channel)
			
			val notification = Notification.Builder(applicationContext, "updateMe")
				.setSmallIcon(R.drawable.notification_default_icon)
				.build()
			val foregroundInfo = if (Build.VERSION.SDK_INT >= 29) {
				ForegroundInfo(0, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
			} else {
				ForegroundInfo(0, notification)
			}
			setForeground(foregroundInfo)
			
		}
		Log.e("updateMe", "started!")
		withContext(Dispatchers.IO) {
			val lastAvatarUrl = HubsDataStore.Auth.getValueFlow(
				applicationContext,
				HubsDataStore.Auth.LastAvatarUrlDownloaded
			).first()
			val avatarFileName = HubsDataStore.Auth.getValueFlow(
				applicationContext,
				HubsDataStore.Auth.AvatarFileName
			).first()
			
			
			val me = MeController.getMe()
			if (me.isSuccess)
				Log.e("updateMe", "Retrieved data successfully!")
			else {
				Log.e("updateMe", "Request failed + ${me.exceptionOrNull()?.message}")
			}
			if (me.isSuccess && me.getOrNull() == null) {
				HubsDataStore.Auth.edit(applicationContext, HubsDataStore.Auth.Alias, "")
				HubsDataStore.Auth.edit(applicationContext, HubsDataStore.Auth.AvatarFileName, "")
				HubsDataStore.Auth.edit(applicationContext, HubsDataStore.Auth.LastAvatarUrlDownloaded, "")
				val avatarFile = File(applicationContext.filesDir, avatarFileName)
				avatarFile.delete()
			}
			me.getOrNull()?.let { me ->
				HubsDataStore.Auth.edit(applicationContext, HubsDataStore.Auth.Alias, me.alias)
				val newAvatarUrl = me.avatarUrl ?: placeholderAvatarUrl(me.alias)
				if (lastAvatarUrl != newAvatarUrl || avatarFileName == "" || !File(applicationContext.filesDir, avatarFileName).exists()) {
					val request = Request.Builder()
						.get()
						.url(newAvatarUrl)
						.build()
					val response = OkHttpClient().newCall(request).execute()
					response.body?.bytes()?.let {
						val filename = "user_avatar" + Calendar.getInstance().time.time + ".png"
						val avatarFile = File(applicationContext.filesDir, filename)
						avatarFile.createNewFile()
						avatarFile.writeBytes(it)
						HubsDataStore.Auth.edit(
							applicationContext,
							HubsDataStore.Auth.AvatarFileName,
							filename
						)
						HubsDataStore.Auth.edit(
							applicationContext,
							HubsDataStore.Auth.LastAvatarUrlDownloaded,
							newAvatarUrl
						)
					}
				}
				Log.e("updateMe", "data saved!")
			}
			
			
		}
		Log.e("updateMe", "ended!")
		
		return Result.success()
	}
	
}