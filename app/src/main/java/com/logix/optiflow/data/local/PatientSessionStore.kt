package com.logix.optiflow.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.logix.optiflow.domain.model.searchbooking.PatientSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.patientSessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "patient_session",
)

@Singleton
class PatientSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.patientSessionDataStore

    suspend fun save(
        patientId: UUID,
        name: String,
        email: String,
        token: String?,
        role: String = PatientSession.PATIENT_ROLE,
    ) {
        dataStore.edit { preferences ->
            preferences[Keys.PATIENT_ID] = patientId.toString()
            preferences[Keys.PATIENT_NAME] = name
            preferences[Keys.PATIENT_EMAIL] = email
            preferences[Keys.ROLE] = role
            if (token.isNullOrBlank()) {
                preferences.remove(Keys.TOKEN)
            } else {
                preferences[Keys.TOKEN] = token
            }
        }
    }

    suspend fun read(): PatientSession? {
        val snapshot = dataStore.data.first()
        val patientId = snapshot[Keys.PATIENT_ID] ?: return null
        return PatientSession(
            patientId = UUID.fromString(patientId),
            token = snapshot[Keys.TOKEN],
            name = snapshot[Keys.PATIENT_NAME].orEmpty(),
            email = snapshot[Keys.PATIENT_EMAIL].orEmpty(),
            role = snapshot[Keys.ROLE] ?: PatientSession.PATIENT_ROLE,
        )
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    fun observeSession() =
        dataStore.data.map { snapshot ->
            val patientId = snapshot[Keys.PATIENT_ID] ?: return@map null
            PatientSession(
                patientId = UUID.fromString(patientId),
                token = snapshot[Keys.TOKEN],
                name = snapshot[Keys.PATIENT_NAME].orEmpty(),
                email = snapshot[Keys.PATIENT_EMAIL].orEmpty(),
                role = snapshot[Keys.ROLE] ?: PatientSession.PATIENT_ROLE,
            )
        }

    private object Keys {
        val PATIENT_ID = stringPreferencesKey("patient_id")
        val TOKEN = stringPreferencesKey("token")
        val PATIENT_NAME = stringPreferencesKey("patient_name")
        val PATIENT_EMAIL = stringPreferencesKey("patient_email")
        val ROLE = stringPreferencesKey("role")
    }
}
