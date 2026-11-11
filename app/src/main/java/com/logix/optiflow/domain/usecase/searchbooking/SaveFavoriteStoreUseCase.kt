package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.repository.searchbooking.OpticalStoreRepository
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import java.util.UUID
import javax.inject.Inject

class SaveFavoriteStoreUseCase @Inject constructor(
    private val opticalStoreRepository: OpticalStoreRepository,
    private val patientRepository: PatientRepository,
) {
    suspend operator fun invoke(opticalStoreId: UUID): Result<Unit> {
        val session = patientRepository.getSession()
            ?: return Result.failure(IllegalStateException("Inicia sesión para guardar la óptica."))
        return opticalStoreRepository.saveFavoriteStore(opticalStoreId, session.patientId)
    }
}
