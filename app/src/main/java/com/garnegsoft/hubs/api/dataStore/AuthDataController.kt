package com.garnegsoft.hubs.api.dataStore

import android.content.Context
import kotlinx.coroutines.flow.Flow

class AuthDataController {
	companion object {
		suspend fun clearAuthData(context: Context) {
			HubsDataStore.Auth.edit(
				context = context,
				pref = HubsDataStore.Auth.Authorized,
				value = false
			)
			HubsDataStore.Auth.edit(
				context = context,
				pref = HubsDataStore.Auth.Cookies,
				value = ""
			)
			HubsDataStore.Auth.edit(
				context = context,
				pref = HubsDataStore.Auth.Alias,
				value = ""
			)
		}
		
		fun isAuthorizedFlow(context: Context): Flow<Boolean> {
			return HubsDataStore.Auth.getValueFlow(
				context = context,
				pref = HubsDataStore.Auth.Authorized)
		}
		
	}
}