package com.garnegsoft.hubs.api.dataStore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.garnegsoft.hubs.api.dataStore.DataStorePreference.Companion.hubsBooleanPreference
import com.garnegsoft.hubs.api.dataStore.DataStorePreference.Companion.hubsFloatPreference
import com.garnegsoft.hubs.api.dataStore.DataStorePreference.Companion.hubsIntPreference
import com.garnegsoft.hubs.api.dataStore.DataStorePreference.Companion.hubsStringPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

object HubsDataStore {
	object Settings : SingleDataStore(name = "settings") {
		object Theme {
			/**
			 * Theme of app.
			 * - 0 - undetermined
			 * - 1 - light
			 * - 2 - dark
			 * - 3 - defined by system
			 *
			 * You also have **mapValues** method for mapping int to enum values
			 */
			object ColorSchemeMode : DataStorePreference<Int> {
				override val key = intPreferencesKey("theme_mode")
				override val defaultValue = ColorScheme.SystemDefined.ordinal
				override val dataStore: SingleDataStore = Settings

				fun mapValues(flow: Flow<Int>): Flow<ColorScheme> {
					return flow.map { ColorScheme.entries[it] }
				}
				
				enum class ColorScheme {
					Undetermined,
					Light,
					Dark,
					SystemDefined
				}
			}
			
		}
		
		object Html {
			object CodeElement {
				val ShowLineNumbers = hubsBooleanPreference("html_code_show_line_numbers", true)
				val ShowLanguage = hubsBooleanPreference("html_code_show_language", true)
			}
		}
		
		object ArticleScreen {
			val FontSize = hubsFloatPreference("article_font_size", 16f)
//			val LineHeightFactor = floatPreferencesKey("line_height_factor")
//			val TextWrapMode = intPreferencesKey("article_text_wrap")
//			val Indent = intPreferencesKey("article_indent")
		}
		
		object ArticleCard {
			val TextSnippetFontSize = hubsFloatPreference("article_card_snippet_font_size", 16f)
			val ShowTextSnippet = hubsBooleanPreference("article_card_show_snippet", true)
			val ShowImage = hubsBooleanPreference("article_card_show_image", true)
			val TitleFontSize = hubsFloatPreference("article_card_title_font_size", 20f)
			val TextSnippetMaxLines = hubsIntPreference("article_card_snippet_max_lines", 4)
		}

	}

	/**
	 * Every preference in this data store represents various flags that define application behaviour
	 * (non-user settings)
	 */
	object applicationFlags : SingleDataStore(name = "app_flags") {
		val ShowSetOpenUrlByDefaultDialog = hubsBooleanPreference("show_set_open_url_by_default_dialog", true)

	}
	
	object Auth : SingleDataStore(name = "auth") {
		val Authorized = hubsBooleanPreference("authorized", false)
		val Cookies = hubsStringPreference("cookies", "")
		val Alias = hubsStringPreference("alias", "")
		val LastAvatarUrlDownloaded = hubsStringPreference("last_avatar_url", "")
		val AvatarFileName = hubsStringPreference("avatar_filename", "")
	}

	object LastRead : SingleDataStore(name = "last_read") {
		
		val LastArticleRead = hubsIntPreference("last_article", 0)
		//val LastArticleReadPosition = intPreferencesKey("last_article_position")
		
	}

	object Filters : SingleDataStore(name = "filters") {
		val MyFeed = hubsStringPreference("my_feed", "")
		val Articles = hubsStringPreference("main_articles", "")
		val News = hubsStringPreference("main_news", "")
	}
	
}



interface DataStorePreference<T> {
	val key: Preferences.Key<T>
	val defaultValue: T
	val dataStore: SingleDataStore


	fun getFlow(context: Context, defaultValue: T = this.defaultValue): Flow<T> {
		return dataStore.getValueFlow(context, this, defaultValue)
	}

	suspend fun edit(context: Context, value: T) {
		dataStore.edit(context, this, value)
	}



	companion object {
		fun SingleDataStore.hubsStringPreference(name: String, defaultValue: String) =
			 StringPreference(name, defaultValue, this)
		
		fun SingleDataStore.hubsBooleanPreference(name: String, defaultValue: Boolean) = 
			BooleanPreference(name, defaultValue, this)

		fun SingleDataStore.hubsFloatPreference(name: String, defaultValue: Float) = 
			FloatPreference(name, defaultValue, this)
		
		fun SingleDataStore.hubsIntPreference(name: String, defaultValue: Int) =
			IntPreference(name, defaultValue, this)

	}

	class FloatPreference(
		name: String,
		override val defaultValue: Float,
		override val dataStore: SingleDataStore
	) : DataStorePreference<Float> {
		override val key = floatPreferencesKey(name)
	}
	
	class BooleanPreference(
		name: String,
		override val defaultValue: Boolean,
		override val dataStore: SingleDataStore
	) : DataStorePreference<Boolean> {
		override val key = booleanPreferencesKey(name)
	}
	
	class StringPreference(
		name: String,
		override val defaultValue: String,
		override val dataStore: SingleDataStore
	) : DataStorePreference<String> {
		override val key = stringPreferencesKey(name)
	}
	
	class IntPreference(
		name: String,
		override val defaultValue: Int,
		override val dataStore: SingleDataStore
	) : DataStorePreference<Int> {
		override val key = intPreferencesKey(name)
	}


	
}

abstract class SingleDataStore(
	private val name: String
) {
	
	private val Context.store by preferencesDataStore(name)
	
	fun <T> getValueFlow(
		context: Context,
		pref: DataStorePreference<T>,
		defaultValue: T = pref.defaultValue
	): Flow<T> {
		return context.store.data.map { it.get(pref.key) ?: defaultValue }
	}
	
	suspend fun <T> edit(context: Context, pref: DataStorePreference<T>, value: T) {
		withContext(Dispatchers.IO) {
			context.store.edit { it.set(pref.key, value) }
		}
	}
}


