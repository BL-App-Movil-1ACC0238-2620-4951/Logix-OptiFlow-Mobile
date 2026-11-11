package com.logix.optiflow.data.repository.searchbooking

import com.logix.optiflow.data.local.PatientSessionStore
import com.logix.optiflow.data.push.PushTokenProvider
import com.logix.optiflow.data.mapper.searchbooking.toDomain
import com.logix.optiflow.data.remote.AuthTokenHolder
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.dto.searchbooking.LoginRequest
import com.logix.optiflow.data.remote.dto.searchbooking.RegisterPatientRequest
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.data.repository.offline.OfflineDataRepositoryImpl
import com.logix.optiflow.domain.model.searchbooking.Patient
import com.logix.optiflow.domain.model.searchbooking.PatientSession
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepositoryImpl @Inject constructor(
    private val api: SearchBookingApi,
    private val sessionStore: PatientSessionStore,
    private val pushTokenProvider: PushTokenProvider,
    private val offline: OfflineDataRepositoryImpl,
) : PatientRepository {

    override suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
    ): Result<Patient> =
        safeApiCall {
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

    override suspend fun registerByStaff(
        name: String,
        email: String,
        phone: String,
        password: String,
        birthDate: String?,
    ): Result<Patient> =
        safeApiCall {
            api.registerPatient(
                RegisterPatientRequest(
                    name = name,
                    email = email,
                    phone = phone,
                    password = password,
                    birthDate = birthDate,
                ),
            ).toDomain()
        }

    override suspend fun login(
        email: String,
        password: String,
    ): Result<PatientSession> =
        safeApiCall {
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

    override suspend fun searchPatients(): Result<List<Patient>> =
        safeApiCall { api.searchPatients().map { it.toDomain() } }

    override suspend fun getPatient(id: UUID): Result<Patient> =
        safeApiCall { api.getPatient(id).toDomain() }

    override suspend fun getSession(): PatientSession? =
        sessionStore.read().also { AuthTokenHolder.token = it?.token }

    override suspend fun clearSession() {
        AuthTokenHolder.token = null
        // Sin sesión el celular ya no debe recibir los push del paciente.
        runCatching { pushTokenProvider.deleteToken() }
        sessionStore.clear()
        // Lo guardado para verse sin conexión es de este usuario.
        offline.clear()
    }
}
