package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.PatientSession
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: PatientRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): PatientSession = repository.login(email, password)
}
