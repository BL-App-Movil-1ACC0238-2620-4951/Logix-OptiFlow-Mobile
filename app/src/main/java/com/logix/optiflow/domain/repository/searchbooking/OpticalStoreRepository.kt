package com.logix.optiflow.domain.repository.searchbooking

import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.domain.model.searchbooking.OpticalStoreSearchResult

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
}
