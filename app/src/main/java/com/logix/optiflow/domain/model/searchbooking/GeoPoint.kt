package com.logix.optiflow.domain.model.searchbooking

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Punto en el mapa (latitud y longitud en grados). */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
) {
    /** Distancia en línea recta (haversine) hasta [other], en kilómetros. */
    fun distanceKmTo(other: GeoPoint): Double {
        val dLat = Math.toRadians(other.latitude - latitude)
        val dLng = Math.toRadians(other.longitude - longitude)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) * sin(dLng / 2).pow(2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(a))
    }

    private companion object {
        const val EARTH_RADIUS_KM = 6371.0
    }
}

/** Ubicación de la óptica, si el backend la envía. */
val OpticalStore.location: GeoPoint?
    get() = if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null
