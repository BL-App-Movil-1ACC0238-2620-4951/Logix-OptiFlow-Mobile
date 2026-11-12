package com.logix.optiflow.domain.repository.searchbooking

import com.logix.optiflow.domain.model.searchbooking.Patient
import com.logix.optiflow.domain.model.searchbooking.PatientSession
import java.util.UUID

interface PatientRepository {
    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
    ): Result<Patient>

    /** El personal registra a un paciente en la óptica; no cambia la sesión actual. */
    suspend fun registerByStaff(
        name: String,
        email: String,
        phone: String,
        password: String,
        birthDate: String?,
    ): Result<Patient>

    /** Inicia sesión de un paciente o del personal y devuelve la sesión con su rol. */
    suspend fun login(
        email: String,
        password: String,
    ): Result<PatientSession>

    suspend fun searchPatients(): Result<List<Patient>>

    suspend fun getPatient(id: UUID): Result<Patient>

    /** El paciente cambia su nombre, teléfono y fecha de nacimiento (`PATCH /patients/{id}`). */
    suspend fun updateProfile(
        name: String,
        phone: String,
        birthDate: String?,
    ): Result<Patient>

    suspend fun getSession(): PatientSession?

    suspend fun clearSession()
}
