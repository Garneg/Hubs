package com.garnegsoft.hubs.api.dataStore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
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
            enum class ThemeModes {
                Undetermined,
                Light,
                Dark,
                SystemDefined
            }
            object ArticleScreen {
                val FontSize = floatPreferencesKey("article_font_size")
                val LineHeightFactor = floatPreferencesKey("line_height_factor")
                val TextWrapMode = intPreferencesKey("article_text_wrap")
                val Indent = intPreferencesKey("article_indent")
            }
            object Comments {
                val CommentsDisplayMode = intPreferencesKey("comment_display_mode")
                enum class CommentsDisplayModes {
                    Default,
                    Threads,
                }
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