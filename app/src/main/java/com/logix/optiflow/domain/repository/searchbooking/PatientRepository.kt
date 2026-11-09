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

    /** Inicia sesión de un paciente o del personal y devuelve la sesión con su rol. */
    suspend fun login(
        email: String,
        password: String,
    ): Result<PatientSession>

    suspend fun searchPatients(): Result<List<Patient>>

    suspend fun getPatient(id: UUID): Result<Patient>

    suspend fun getSession(): PatientSession?

    suspend fun clearSession()
}
