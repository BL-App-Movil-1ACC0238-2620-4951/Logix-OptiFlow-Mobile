package com.logix.optiflow.presentation.search.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.logix.optiflow.R
import com.logix.optiflow.domain.model.searchbooking.GeoPoint
import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.domain.model.searchbooking.location
import java.io.File
import java.util.UUID
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint as MapPoint

/** Centro de Lima, para cuando no hay ubicación del usuario ni ópticas con coordenadas. */
private val LIMA = MapPoint(-12.1, -77.03)

private const val ZOOM = 12.0

val LOCATION_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

fun hasLocationPermission(context: Context): Boolean =
    LOCATION_PERMISSIONS.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

/**
 * Mapa de OpenStreetMap (osmdroid) con un pin por óptica en sus coordenadas del backend y un
 * punto azul en la ubicación del usuario. Con [interactive] en false es la vista previa de la
 * búsqueda: no se mueve y cualquier toque llama a [onMapClick].
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
fun StoreMap(
    stores: List<OpticalStore>,
    userLocation: GeoPoint?,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    selectedStoreId: UUID? = null,
    onStoreClick: (OpticalStore) -> Unit = {},
    onMapClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val mapView = remember { createMapView(context, interactive) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    val located = stores.mapNotNull { store -> store.location?.let { store to MapPoint(it.latitude, it.longitude) } }
    val center =
        userLocation?.let { MapPoint(it.latitude, it.longitude) }
            ?: located.takeIf { it.isNotEmpty() }?.let { points ->
                MapPoint(points.map { it.second.latitude }.average(), points.map { it.second.longitude }.average())
            }
            ?: LIMA
    LaunchedEffect(center.latitude, center.longitude) { mapView.controller.setCenter(center) }

    AndroidView(
        factory = { mapView },
        // MapView dibuja fuera de sus bordes si no se recorta.
        modifier = modifier.clipToBounds(),
        update = { map ->
            if (!interactive) {
                map.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) onMapClick()
                    true
                }
            }
            map.overlays.removeAll { it is Marker }
            located.forEach { (store, point) ->
                map.overlays.add(
                    Marker(map).apply {
                        position = point
                        title = store.name
                        snippet = store.address
                        icon = ContextCompat.getDrawable(context, R.drawable.map_store_pin)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        alpha = if (selectedStoreId == null || selectedStoreId == store.id) 1f else 0.6f
                        setOnMarkerClickListener { _, _ ->
                            onStoreClick(store)
                            true
                        }
                    },
                )
            }
            userLocation?.let {
                map.overlays.add(
                    Marker(map).apply {
                        position = MapPoint(it.latitude, it.longitude)
                        icon = userDot()
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        setInfoWindow(null)
                        setOnMarkerClickListener { _, _ -> true }
                    },
                )
            }
            map.invalidate()
        },
    )
}

private fun createMapView(context: Context, interactive: Boolean): MapView {
    // OpenStreetMap pide identificar la app; los mosaicos se guardan en la carpeta privada de la app.
    Configuration.getInstance().apply {
        userAgentValue = context.packageName
        osmdroidBasePath = File(context.filesDir, "osmdroid")
        osmdroidTileCache = File(context.cacheDir, "osmdroid")
    }
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(interactive)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        isTilesScaledToDpi = true
        controller.setZoom(ZOOM)
        controller.setCenter(LIMA)
        // Crédito obligatorio de OpenStreetMap.
        overlays.add(CopyrightOverlay(context))
    }
}

/** Punto azul con borde blanco para la ubicación del usuario. */
private fun userDot(): GradientDrawable =
    GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(0xFF1A73E8.toInt())
        setStroke(6, 0xFFFFFFFF.toInt())
        setSize(44, 44)
    }
