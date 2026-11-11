package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.GeoPoint
import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.domain.repository.searchbooking.OpticalStoreRepository
import javax.inject.Inject

class GetNearbyOpticalStoresUseCase @Inject constructor(
    private val repository: OpticalStoreRepository,
) {
    suspend operator fun invoke(point: GeoPoint, radiusKm: Double = DEFAULT_RADIUS_KM): Result<List<OpticalStore>> =
        repository.getNearbyOpticalStores(point.latitude, point.longitude, radiusKm)

    companion object {
        const val DEFAULT_RADIUS_KM = 5.0
    }
}
