package com.garnegsoft.hubs.api.dataStore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext


@Composable
fun <T> collectPreferenceAsState(preference: DataStorePreference<T>, initial: T? = null): State<T?> {
    val context = LocalContext.current
    return preference.getFlow(context).collectAsState(initial = initial)
}