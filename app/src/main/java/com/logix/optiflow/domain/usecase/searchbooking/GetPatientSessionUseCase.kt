package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.PatientSession
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import javax.inject.Inject

class GetPatientSessionUseCase @Inject constructor(
    private val repository: PatientRepository,
) {
    suspend operator fun invoke(): PatientSession? = repository.getSession()
}
