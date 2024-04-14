package com.garnegsoft.hubs.data.dataStore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
				
				fun mapValues(flow: Flow<Int>): Flow<ColorScheme> {
					return flow.map { ColorScheme.values()[it] }
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
				val ShowLineNumbers = DataStorePreference.BooleanPreference("html_code_show_line_numbers", true)
				val ShowLanguage = DataStorePreference.BooleanPreference("html_code_show_language", true)
			}
		}
		
		object ArticleScreen {
			val FontSize = DataStorePreference.FloatPreference("article_font_size", 16f)
//			val LineHeightFactor = floatPreferencesKey("line_height_factor")
//			val TextWrapMode = intPreferencesKey("article_text_wrap")
//			val Indent = intPreferencesKey("article_indent")
		}
		
		object ArticleCard {
			val TextSnippetFontSize = DataStorePreference.FloatPreference("article_card_snippet_font_size", 16f)
			val ShowTextSnippet = DataStorePreference.BooleanPreference("article_card_show_snippet", true)
			val ShowImage = DataStorePreference.BooleanPreference("article_card_show_image", true)
			val TitleFontSize = DataStorePreference.FloatPreference("article_card_title_font_size", 20f)
			val TextSnippetMaxLines = DataStorePreference.IntPreference("article_card_snippet_max_lines", 4)
		}
		
		object CommentsDisplayMode : DataStorePreference<Int> {
			
			override val key = intPreferencesKey("comment_display_mode")
			
			override val defaultValue = CommentsDisplayModes.Default.ordinal
			
			enum class CommentsDisplayModes {
				Default,
				Threads,
			}
		}
	}
	
	object Auth : SingleDataStore(name = "auth") {
		val Authorized = DataStorePreference.BooleanPreference("authorized", false)
		val Cookies = DataStorePreference.StringPreference("cookies", "")
		val Alias = DataStorePreference.StringPreference("alias", "")
		val LastAvatarUrlDownloaded = DataStorePreference.StringPreference("last_avatar_url", "")
		val AvatarFileName = DataStorePreference.StringPreference("avatar_filename", "")
	}
	
	object LastRead : SingleDataStore(name = "last_read") {
		
		val LastArticleRead = DataStorePreference.IntPreference("last_article", 0)
		//val LastArticleReadPosition = intPreferencesKey("last_article_position")
		
	}
	
}

interface DataStorePreference<T> {
	val key: Preferences.Key<T>
	val defaultValue: T
	
	class FloatPreference(
		name: String,
		override val defaultValue: Float
	) : DataStorePreference<Float> {
		override val key = floatPreferencesKey(name)
	}
	
	class BooleanPreference(
		name: String,
		override val defaultValue: Boolean
	) : DataStorePreference<Boolean> {
		override val key = booleanPreferencesKey(name)
	}
	
	class StringPreference(
		name: String,
		override val defaultValue: String
	) : DataStorePreference<String> {
		override val key = stringPreferencesKey(name)
	}
	
	class IntPreference(
		name: String,
		override val defaultValue: Int
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
		context.store.edit { it.set(pref.key, value) }
	}
}


