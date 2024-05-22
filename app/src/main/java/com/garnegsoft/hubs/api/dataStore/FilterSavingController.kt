package com.garnegsoft.hubs.api.dataStore

import android.content.Context
import android.util.Log
import com.garnegsoft.hubs.ui.screens.main.MyFeedFilter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.single
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class FilterSavingController {
	companion object {
		suspend fun saveMyFeedFilter(context: Context, filterPreferences: MyFeedFilter) {
			HubsDataStore.FiltersPreferences.edit(
				context,
				HubsDataStore.FiltersPreferences.MyFeed,
				Json.encodeToString(filterPreferences)
			)
		}
		
		suspend fun getMyFeedFilter(context: Context, defaultFilterPreferences: MyFeedFilter) : MyFeedFilter {
			return HubsDataStore.FiltersPreferences.getValueFlow(
				context,
				HubsDataStore.FiltersPreferences.MyFeed
			).firstOrNull()?.let {
				return@let try {
					Json.decodeFromString(it)
				} catch (ex: Exception) {
					Log.e("filter_serialization", "Unable to deserialize filter. Exception: $ex")
					null
				}
			} ?: defaultFilterPreferences
			
		}
	}
}