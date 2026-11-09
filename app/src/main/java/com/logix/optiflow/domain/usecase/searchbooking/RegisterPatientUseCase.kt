package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.Patient
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import javax.inject.Inject

class RegisterPatientUseCase @Inject constructor(
    private val repository: PatientRepository,
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        phone: String,
        password: String,
    ): Patient = repository.register(name, email, phone, password)
}
