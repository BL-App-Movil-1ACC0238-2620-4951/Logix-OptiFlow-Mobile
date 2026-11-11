package com.logix.optiflow.domain.repository.searchbooking

import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.domain.model.searchbooking.OpticalStoreSearchResult
import java.util.UUID

interface OpticalStoreRepository {
    suspend fun getOpticalStores(
        name: String? = null,
        address: String? = null,
    ): Result<List<OpticalStore>>

    suspend fun searchOpticalStores(
        name: String? = null,
        address: String? = null,
        minRating: Double? = null,
    ): Result<OpticalStoreSearchResult>

    /** Califica una óptica (de 1 a 5) y devuelve su nuevo promedio. */
    suspend fun rateOpticalStore(
        opticalStoreId: UUID,
        patientId: UUID,
        score: Int,
        comment: String?,
    ): Result<Double?>

    /** Guarda una óptica como favorita del paciente. */
    suspend fun saveFavoriteStore(opticalStoreId: UUID, patientId: UUID): Result<Unit>
}
