package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.Patient
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import javax.inject.Inject

class RegisterPatientByStaffUseCase @Inject constructor(
    private val repository: PatientRepository,
) {
    /** El DNI queda como contraseña temporal del paciente. */
    suspend operator fun invoke(
        name: String,
        email: String,
        phone: String,
        dni: String,
        birthDate: String?,
    ): Result<Patient> = repository.registerByStaff(name, email, phone, dni, birthDate)
}
