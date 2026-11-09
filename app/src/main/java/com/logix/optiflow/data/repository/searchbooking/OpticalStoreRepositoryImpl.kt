package com.logix.optiflow.data.repository.searchbooking

import com.logix.optiflow.data.mapper.searchbooking.toDomain
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.domain.model.searchbooking.OpticalStoreSearchResult
import com.logix.optiflow.domain.repository.searchbooking.OpticalStoreRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpticalStoreRepositoryImpl @Inject constructor(
    private val api: SearchBookingApi,
) : OpticalStoreRepository {

    override suspend fun getOpticalStores(
        name: String?,
        address: String?,
    ): Result<List<OpticalStore>> =
        safeApiCall {
            api
                .listOpticalStores(name = name, address = address)
                .map { it.toDomain() }
        }

    override suspend fun searchOpticalStores(
        name: String?,
        address: String?,
        minRating: Double?,
    ): Result<OpticalStoreSearchResult> =
        safeApiCall {
            val stores =
                api
                    .listOpticalStores(name = name, address = address, minRating = minRating)
                    .map { it.toDomain() }
            OpticalStoreSearchResult(
                stores = stores,
                message = if (stores.isEmpty()) NO_RESULTS_MESSAGE else null,
            )
        }

    private companion object {
        const val NO_RESULTS_MESSAGE = "No se encontraron ópticas con esos criterios."
    }
}
