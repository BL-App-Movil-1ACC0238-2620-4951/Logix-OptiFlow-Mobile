package com.logix.optiflow.data.local.inventory

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.favoriteFrameDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "favorite_frames",
)

/** SKUs de las monturas favoritas, guardados en el celular para cada paciente. */
@Singleton
class FavoriteFrameStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.favoriteFrameDataStore

    fun favorites(patientId: UUID): Flow<Set<String>> =
        dataStore.data.map { it[key(patientId)].orEmpty() }

    suspend fun toggle(patientId: UUID, sku: String) {
        dataStore.edit { prefs ->
            val current = prefs[key(patientId)].orEmpty()
            prefs[key(patientId)] = if (sku in current) current - sku else current + sku
        }
    }

    private fun key(patientId: UUID) = stringSetPreferencesKey("favorites_$patientId")
}
