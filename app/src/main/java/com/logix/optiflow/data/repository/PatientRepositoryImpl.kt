package com.logix.optiflow.data.repository

import com.logix.optiflow.data.local.PatientSessionStore
import com.logix.optiflow.data.mapper.toDomain
import com.logix.optiflow.data.remote.AuthTokenHolder
import com.logix.optiflow.data.remote.api.SearchBookingApi
import com.logix.optiflow.data.remote.dto.LoginRequest
import com.logix.optiflow.data.remote.dto.RegisterPatientRequest
import com.logix.optiflow.domain.model.Patient
import com.logix.optiflow.domain.model.PatientSession
import com.logix.optiflow.domain.repository.PatientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PatientRepositoryImpl(
    private val api: SearchBookingApi,
    private val sessionStore: PatientSessionStore,
) : PatientRepository {

    override suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
    ): Patient =
        withContext(Dispatchers.IO) {
            val patient =
                api
                    .registerPatient(
                        RegisterPatientRequest(
                            name = name,
                            email = email,
                            phone = phone,
                            password = password,
                        ),
                    )
                    .toDomain()
            sessionStore.save(
                patientId = patient.id,
                name = patient.name,
                email = patient.email,
                token = null,
            )
            patient
        }

    override suspend fun login(
        email: String,
        password: String,
    ): PatientSession =
        withContext(Dispatchers.IO) {
            val response = api.login(LoginRequest(email = email, password = password))
            AuthTokenHolder.token = response.token
            val profile =
                if (response.role == PatientSession.PATIENT_ROLE) {
                    api.getPatient(response.userId).toDomain()
                } else {
                    null
                }
            val session =
                PatientSession(
                    patientId = response.userId,
                    token = response.token,
                    name = profile?.name.orEmpty(),
                    email = profile?.email ?: email,
                    role = response.role,
                )
            sessionStore.save(
                patientId = session.patientId,
                name = session.name,
                email = session.email,
                token = session.token,
                role = session.role,
            )
            session
        }

    override suspend fun getSession(): PatientSession? =
        sessionStore.read().also { AuthTokenHolder.token = it?.token }

    override suspend fun clearSession() {
        AuthTokenHolder.token = null
        sessionStore.clear()
    }
}
