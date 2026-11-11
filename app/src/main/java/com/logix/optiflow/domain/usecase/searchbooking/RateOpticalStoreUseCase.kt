package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.repository.searchbooking.OpticalStoreRepository
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import java.util.UUID
import javax.inject.Inject

class RateOpticalStoreUseCase @Inject constructor(
    private val opticalStoreRepository: OpticalStoreRepository,
    private val patientRepository: PatientRepository,
) {
    suspend operator fun invoke(opticalStoreId: UUID, score: Int, comment: String?): Result<Double?> {
        val session = patientRepository.getSession()
            ?: return Result.failure(IllegalStateException("Inicia sesión para calificar la óptica."))
        return opticalStoreRepository.rateOpticalStore(opticalStoreId, session.patientId, score, comment)
    }
}
