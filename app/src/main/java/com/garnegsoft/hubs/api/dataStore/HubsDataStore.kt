package com.garnegsoft.hubs.api.dataStore

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
		
		object ArticleScreen {
			val FontSize = DataStorePreference.FloatPreference("article_font_size", 16f)
//			val LineHeightFactor = floatPreferencesKey("line_height_factor")
//			val TextWrapMode = intPreferencesKey("article_text_wrap")
//			val Indent = intPreferencesKey("article_indent")
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
	
	object LegacySettings {
		const val DataStoreName = "settings"
		
		
		object Keys {
			
			
		
			
			
		}
	}
	
	object Auth {
		const val DataStoreName = "auth"
		
		object Keys {
			val Authorized = booleanPreferencesKey("authorized")
			val Cookies = stringPreferencesKey("cookies")
		}
		
		
	}
	
	object LastRead {
		const val DataStoreName = "last_read"
		
		object Keys {
			val LastArticleRead = intPreferencesKey("last_article")
			val LastArticleReadPosition = intPreferencesKey("last_article_position")
		}
	}
	
}

interface DataStorePreference<T> {
	val key: Preferences.Key<T>
	val defaultValue: T
	
	class FloatPreference(
		name: String,
		override val defaultValue: Float
	) : DataStorePreference<Float> {
		override val key: Preferences.Key<Float> = floatPreferencesKey(name)
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


