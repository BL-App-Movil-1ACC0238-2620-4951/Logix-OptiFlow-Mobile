package com.logix.optiflow.domain.model

import java.util.UUID

data class Patient(
    val id: UUID,
    val name: String,
    val email: String,
    val phone: String,
)

/**
 * Sesión iniciada. Para el personal, [patientId] guarda el identificador del usuario y
 * [role] su rol (`OPTOMETRIST`, `SALES_ADVISOR`, `TECHNICIAN` o `ADMIN`).
 */
data class PatientSession(
    val patientId: UUID,
    val token: String?,
    val name: String,
    val email: String,
    val role: String = PATIENT_ROLE,
) {
    val isPatient: Boolean get() = role == PATIENT_ROLE

    companion object {
        const val PATIENT_ROLE = "PATIENT"
    }
}
