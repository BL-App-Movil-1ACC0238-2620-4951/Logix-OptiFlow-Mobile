package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.domain.repository.searchbooking.OpticalStoreRepository
import javax.inject.Inject

class GetOpticalStoresUseCase @Inject constructor(
    private val repository: OpticalStoreRepository,
) {
    suspend operator fun invoke(
        name: String? = null,
        address: String? = null,
    ): List<OpticalStore> = repository.getOpticalStores(name, address)
}
