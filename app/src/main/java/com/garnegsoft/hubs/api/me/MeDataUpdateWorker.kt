package com.garnegsoft.hubs.api.me

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
			if (me == null) {
				HubsDataStore.Auth.edit(applicationContext, HubsDataStore.Auth.Alias, "")
				HubsDataStore.Auth.edit(applicationContext, HubsDataStore.Auth.AvatarFileName, "")
				val avatarFile = File(applicationContext.filesDir, avatarFileName)
				avatarFile.delete()
			}
			me?.let { me ->
				HubsDataStore.Auth.edit(applicationContext, HubsDataStore.Auth.Alias, me.alias)
				val newAvatarUrl = me.avatarUrl ?: placeholderAvatarUrl(me.alias)
				if (lastAvatarUrl != newAvatarUrl) {
					val request = Request.Builder()
						.get()
						.url(newAvatarUrl)
						.build()
					val response = OkHttpClient().newCall(request).execute()
					response.body?.bytes()?.let {
						val filename = "user_avatar" + Calendar.getInstance().get(Calendar.SECOND) + ".png"
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
				
			}
			
			
		}
		
		
		return Result.success()
	}
	
}