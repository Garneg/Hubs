package com.garnegsoft.hubs.api.dataStore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import com.garnegsoft.hubs.authDataStore
import com.garnegsoft.hubs.lastReadDataStore
import com.garnegsoft.hubs.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <T> Context.lastReadDataStoreFlow(key: Preferences.Key<T>): Flow<T?> {
	return lastReadDataStore.data.map { it[key] }
}

fun <T> Context.settingsDataStoreFlow(key: Preferences.Key<T>): Flow<T?> {
	return settingsDataStore.data.map { it[key] }
}

fun <T> Context.settingsDataStoreFlowWithDefault(
	key: Preferences.Key<T>,
	defaultValue: T
): Flow<T> {
	return settingsDataStore.data.map { it[key] ?: defaultValue }
}

fun <T> Context.authDataStoreFlow(key: Preferences.Key<T>): Flow<T?> {
	return authDataStore.data.map { it[key] }
}

fun <T> Context.authDataStoreFlowWithDefault(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
	return authDataStore.data.map { it[key] ?: defaultValue }
}