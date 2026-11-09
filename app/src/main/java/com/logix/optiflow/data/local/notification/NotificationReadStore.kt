package com.logix.optiflow.data.local.notification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.notificationReadDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "notification_read",
)

@Singleton
class NotificationReadStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.notificationReadDataStore

    suspend fun readIds(): Set<String> {
        val snapshot = dataStore.data.first()
        return snapshot[Keys.READ_IDS].orEmpty()
    }

    suspend fun markAll(ids: Collection<String>) {
        dataStore.edit { prefs ->
            val merged = prefs[Keys.READ_IDS].orEmpty().toMutableSet()
            merged.addAll(ids)
            prefs[Keys.READ_IDS] = merged
        }
    }

    suspend fun markOne(id: String) {
        dataStore.edit { prefs ->
            val merged = prefs[Keys.READ_IDS].orEmpty().toMutableSet()
            merged.add(id)
            prefs[Keys.READ_IDS] = merged
        }
    }

    private object Keys {
        val READ_IDS = stringSetPreferencesKey("read_notification_ids")
    }
}
