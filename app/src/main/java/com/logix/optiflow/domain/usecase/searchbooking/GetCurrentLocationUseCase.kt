package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.GeoPoint
import com.logix.optiflow.domain.repository.searchbooking.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(): GeoPoint? = repository.getCurrentLocation()
}
