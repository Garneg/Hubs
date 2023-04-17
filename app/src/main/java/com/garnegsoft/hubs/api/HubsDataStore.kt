package com.garnegsoft.hubs.api

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object HubsDataStore {
    object Settings {
        const val DataStoreName = "settings"
        object Keys {
            /**
             * Theme of app.
             * - 0 - undetermined
             * - 1 - light
             * - 2 - dark
             * - 3 - defined by system
             */
            val Theme = intPreferencesKey("theme_mode")
            enum class ThemeMode {
                Undetermined,
                Light,
                Dark,
                SystemDefined
            }
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
        object Keys{
            val LastArticleRead = intPreferencesKey("last_article")
            val LastArticleReadPosition = intPreferencesKey("last_article_position")
        }
    }

}