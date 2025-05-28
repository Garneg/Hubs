package com.garnegsoft.hubs.api.dataStore

import android.content.Context
import android.util.Log
import com.garnegsoft.hubs.ui.screens.main.ArticlesFilter
import com.garnegsoft.hubs.ui.screens.main.MyFeedFilter
import com.garnegsoft.hubs.ui.screens.main.NewsFilter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class FilterSavingController {
	companion object {
		suspend fun saveMyFeedFilter(context: Context, filterPreferences: MyFeedFilter) {
			HubsDataStore.Filters.edit(
				context,
				HubsDataStore.Filters.MyFeed,
				Json.encodeToString(filterPreferences)
			)
		}
		
		suspend fun getMyFeedFilter(context: Context, defaultFilterPreferences: MyFeedFilter) : MyFeedFilter {
			HubsDataStore.applicationFlags
			return HubsDataStore.Filters.MyFeed.getFlow(context).firstOrNull()?.let {
				return@let try {
					Json.decodeFromString(it)
				} catch (ex: Exception) {
					Log.e("filter_serialization", "Unable to deserialize filter. Exception: $ex")
					null
				}
			} ?: defaultFilterPreferences
			
		}
	
		suspend fun saveArticlesFilter(context: Context, filterPreferences: ArticlesFilter) {
			HubsDataStore.Filters.Articles.edit(
				context,
				Json.encodeToString(filterPreferences)
			)
		}
		
		suspend fun getArticlesFilter(context: Context, defaultFilterPreferences: ArticlesFilter) : ArticlesFilter {
			return HubsDataStore.Filters.getValueFlow(
				context,
				HubsDataStore.Filters.Articles
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
			HubsDataStore.Filters.edit(
				context,
				HubsDataStore.Filters.News,
				Json.encodeToString(filterPreferences)
			)
		}
		
		suspend fun getNewsFilter(context: Context, defaultFilterPreferences: NewsFilter) : NewsFilter {
			return HubsDataStore.Filters.getValueFlow(
				context,
				HubsDataStore.Filters.News
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