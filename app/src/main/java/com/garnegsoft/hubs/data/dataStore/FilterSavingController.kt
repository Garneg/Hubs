package com.garnegsoft.hubs.api.dataStore

import android.content.Context
import android.util.Log
import com.garnegsoft.hubs.data.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.screens.main.ArticlesFilter
import com.garnegsoft.hubs.ui.screens.main.MyFeedFilter
import com.garnegsoft.hubs.ui.screens.main.NewsFilter
import kotlinx.coroutines.flow.firstOrNull
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
	
		suspend fun saveArticlesFilter(context: Context, filterPreferences: ArticlesFilter) {
			HubsDataStore.FiltersPreferences.edit(
				context,
				HubsDataStore.FiltersPreferences.Articles,
				Json.encodeToString(filterPreferences)
			)
		}
		
		suspend fun getArticlesFilter(context: Context, defaultFilterPreferences: ArticlesFilter) : ArticlesFilter {
			return HubsDataStore.FiltersPreferences.getValueFlow(
				context,
				HubsDataStore.FiltersPreferences.Articles
			).firstOrNull()?.let {
				return@let try {
					Json.decodeFromString(it)
				} catch (ex: Exception) {
					Log.e("filter_serialization", "Unable to deserialize filter. Exception: $ex")
					null
				}
			} ?: defaultFilterPreferences
		}
		
		suspend fun saveNewsFilter(context: Context, filterPreferences: NewsFilter) {
			HubsDataStore.FiltersPreferences.edit(
				context,
				HubsDataStore.FiltersPreferences.News,
				Json.encodeToString(filterPreferences)
			)
		}
		
		suspend fun getNewsFilter(context: Context, defaultFilterPreferences: NewsFilter) : NewsFilter {
			return HubsDataStore.FiltersPreferences.getValueFlow(
				context,
				HubsDataStore.FiltersPreferences.News
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