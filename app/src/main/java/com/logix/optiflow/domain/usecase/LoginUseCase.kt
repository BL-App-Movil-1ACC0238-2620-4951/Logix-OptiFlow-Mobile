package com.logix.optiflow.domain.usecase

import com.logix.optiflow.domain.model.PatientSession
import com.logix.optiflow.domain.repository.PatientRepository

class LoginUseCase(
    private val repository: PatientRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): PatientSession = repository.login(email, password)
}
