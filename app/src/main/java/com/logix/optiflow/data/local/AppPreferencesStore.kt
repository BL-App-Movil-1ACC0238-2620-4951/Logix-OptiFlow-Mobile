package com.logix.optiflow.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences",
)

/** Preferencias de Configuración que se guardan en este celular. */
@Singleton
class AppPreferencesStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.appPreferencesDataStore

    val checkupReminders: Flow<Boolean> = dataStore.data.map { it[CHECKUP_REMINDERS] ?: false }

    suspend fun setCheckupReminders(enabled: Boolean) {
        dataStore.edit { it[CHECKUP_REMINDERS] = enabled }
    }

    private companion object {
        val CHECKUP_REMINDERS = booleanPreferencesKey("checkup_reminders")
    }
}
