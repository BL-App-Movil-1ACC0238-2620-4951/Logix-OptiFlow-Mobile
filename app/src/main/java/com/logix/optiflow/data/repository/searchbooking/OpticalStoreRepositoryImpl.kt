package com.logix.optiflow.data.repository.searchbooking

import com.logix.optiflow.data.mapper.searchbooking.toDomain
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.dto.searchbooking.OpticalStoreDto
import com.logix.optiflow.data.remote.dto.searchbooking.RateOpticalStoreRequest
import com.logix.optiflow.data.remote.dto.searchbooking.SaveFavoriteStoreRequest
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.data.repository.offline.OfflineDataRepositoryImpl
import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.domain.model.searchbooking.OpticalStoreSearchResult
import com.logix.optiflow.domain.repository.searchbooking.OpticalStoreRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpticalStoreRepositoryImpl @Inject constructor(
    private val api: SearchBookingApi,
    private val offline: OfflineDataRepositoryImpl,
) : OpticalStoreRepository {

    override suspend fun getOpticalStores(
        name: String?,
        address: String?,
    ): Result<List<OpticalStore>> =
        if (name == null && address == null) {
            // La lista completa se guarda para verse sin conexión; las búsquedas con filtro no.
            offline.list("optical-stores", OpticalStoreDto::class.java) { api.listOpticalStores() }
                .map { dtos -> dtos.map { it.toDomain() } }
        } else {
            safeApiCall {
                api
                    .listOpticalStores(name = name, address = address)
                    .map { it.toDomain() }
            }
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

    override suspend fun getNearbyOpticalStores(
        latitude: Double,
        longitude: Double,
        radiusKm: Double,
    ): Result<List<OpticalStore>> =
        safeApiCall {
            api
                .listOpticalStores(latitude = latitude, longitude = longitude, radiusKm = radiusKm)
                .map { it.toDomain() }
        }

    override suspend fun rateOpticalStore(
        opticalStoreId: UUID,
        patientId: UUID,
        score: Int,
        comment: String?,
    ): Result<Double?> =
        safeApiCall {
            api.rateOpticalStore(
                opticalStoreId,
                RateOpticalStoreRequest(patientId, score.toDouble(), comment?.trim()?.takeIf { it.isNotEmpty() }),
            ).averageRating
        }

    override suspend fun saveFavoriteStore(opticalStoreId: UUID, patientId: UUID): Result<Unit> =
        safeApiCall { api.saveFavoriteStore(patientId, SaveFavoriteStoreRequest(opticalStoreId)) }

    private companion object {
        const val NO_RESULTS_MESSAGE = "No se encontraron ópticas con esos criterios."
    }
}
