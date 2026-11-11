package com.logix.optiflow.domain.repository.searchbooking

import com.logix.optiflow.domain.model.searchbooking.GeoPoint

interface LocationRepository {
    /** Ubicación actual del celular; null si no hay permiso o no se pudo obtener. */
    suspend fun getCurrentLocation(): GeoPoint?
}
