package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.PatientSession
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository

class GetPatientSessionUseCase(
    private val repository: PatientRepository,
) {
    suspend operator fun invoke(): PatientSession? = repository.getSession()
}
