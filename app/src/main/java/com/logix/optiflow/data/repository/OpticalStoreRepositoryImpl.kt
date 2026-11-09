package com.logix.optiflow.data.repository

import com.logix.optiflow.data.mapper.toDomain
import com.logix.optiflow.data.remote.api.SearchBookingApi
import com.logix.optiflow.domain.model.OpticalStore
import com.logix.optiflow.domain.model.OpticalStoreSearchResult
import com.logix.optiflow.domain.repository.OpticalStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpticalStoreRepositoryImpl(
    private val api: SearchBookingApi,
) : OpticalStoreRepository {

    override suspend fun getOpticalStores(
        name: String?,
        address: String?,
    ): List<OpticalStore> =
        withContext(Dispatchers.IO) {
            api
                .listOpticalStores(name = name, address = address)
                .map { it.toDomain() }
        }

    override suspend fun searchOpticalStores(
        name: String?,
        address: String?,
        minRating: Double?,
    ): OpticalStoreSearchResult =
        withContext(Dispatchers.IO) {
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
