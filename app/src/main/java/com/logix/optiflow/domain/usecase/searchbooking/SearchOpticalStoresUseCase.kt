package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.OpticalStoreSearchResult
import com.logix.optiflow.domain.repository.searchbooking.OpticalStoreRepository
import javax.inject.Inject

class SearchOpticalStoresUseCase @Inject constructor(
    private val repository: OpticalStoreRepository,
) {
    suspend operator fun invoke(
        name: String? = null,
        address: String? = null,
        minRating: Double? = null,
    ): Result<OpticalStoreSearchResult> = repository.searchOpticalStores(name, address, minRating)
}
