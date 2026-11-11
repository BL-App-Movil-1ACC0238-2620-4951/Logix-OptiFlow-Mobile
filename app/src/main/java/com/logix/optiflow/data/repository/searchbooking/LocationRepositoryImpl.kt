package com.logix.optiflow.data.repository.searchbooking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import com.logix.optiflow.domain.model.searchbooking.GeoPoint
import com.logix.optiflow.domain.repository.searchbooking.LocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationRepository {

    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): GeoPoint? {
        if (!hasPermission()) return null
        // En algunos equipos la ubicación actual no llega nunca; tras unos segundos se usa la última conocida.
        val current = withTimeoutOrNull(CURRENT_TIMEOUT_MS) {
            await { client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null) }
        }
        return (current ?: await { client.lastLocation })?.let { GeoPoint(it.latitude, it.longitude) }
    }

    /** Espera la tarea de Play Services; si falla devuelve null. */
    private suspend fun await(task: () -> Task<Location?>): Location? =
        suspendCancellableCoroutine { continuation ->
            task()
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
        }

    private fun hasPermission(): Boolean =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    private companion object {
        const val CURRENT_TIMEOUT_MS = 5_000L
    }
}
