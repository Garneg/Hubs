package com.garnegsoft.hubs.api

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object DataStoreKeys {
    object Settings{

    }
    object Auth{
        val Authorized = booleanPreferencesKey("authorized")
        val Cookies = stringPreferencesKey("cookies")
        val UserAlias = stringPreferencesKey("alias")
        val UserAvatarFile = stringPreferencesKey("avatar_file")

    }
    object Saved{
        val Articles = stringSetPreferencesKey("saved_articles")

    }
}