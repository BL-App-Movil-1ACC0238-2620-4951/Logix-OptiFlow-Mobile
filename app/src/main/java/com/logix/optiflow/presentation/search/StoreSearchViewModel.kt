package com.logix.optiflow.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.searchbooking.GeoPoint
import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.domain.model.searchbooking.OpticalStoreSearchResult
import com.logix.optiflow.domain.model.searchbooking.location
import com.logix.optiflow.domain.usecase.searchbooking.GetCurrentLocationUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetFirstAvailableSlotsUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetNearbyOpticalStoresUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetOpticalStoresUseCase
import com.logix.optiflow.domain.usecase.searchbooking.SaveFavoriteStoreUseCase
import com.logix.optiflow.domain.usecase.searchbooking.SearchOpticalStoresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

/** Chips de la búsqueda: todas, las cercanas (`lat`/`lng`/`radiusKm`) o por primer turno libre. */
enum class StoreFilter(val label: String) {
    ALL("Todas"),
    NEARBY("Cerca de mí"),
    FIRST_SLOT("Primera hora"),
}

data class StoreSearchUiState(
    val nameQuery: String = "",
    val addressQuery: String = "",
    val minRatingQuery: String = "",
    val useAdvancedFilter: Boolean = false,
    val stores: List<OpticalStore> = emptyList(),
    val infoMessage: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val favoriteStoreIds: Set<UUID> = emptySet(),
    val filter: StoreFilter = StoreFilter.ALL,
    val userLocation: GeoPoint? = null,
    /** Primer turno libre de cada óptica. */
    val firstSlots: Map<UUID, Instant> = emptyMap(),
) {
    fun distanceKm(store: OpticalStore): Double? =
        userLocation?.let { user -> store.location?.let(user::distanceKmTo) }

    /** Ópticas en el orden del chip elegido: por distancia si hay ubicación, o por primer turno. */
    val visibleStores: List<OpticalStore>
        get() = when (filter) {
            StoreFilter.FIRST_SLOT -> stores.sortedBy { firstSlots[it.id] ?: Instant.MAX }
            else -> if (userLocation == null) stores else stores.sortedBy { distanceKm(it) ?: Double.MAX_VALUE }
        }
}

@HiltViewModel
class StoreSearchViewModel @Inject constructor(
    private val getOpticalStores: GetOpticalStoresUseCase,
    private val searchOpticalStores: SearchOpticalStoresUseCase,
    private val saveFavoriteStore: SaveFavoriteStoreUseCase,
    private val getNearbyOpticalStores: GetNearbyOpticalStoresUseCase,
    private val getCurrentLocation: GetCurrentLocationUseCase,
    private val getFirstAvailableSlots: GetFirstAvailableSlotsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreSearchUiState())
    val uiState: StateFlow<StoreSearchUiState> = _uiState.asStateFlow()

    init {
        search()
    }

    /** Se llama al abrir la pantalla y tras el permiso de ubicación; sin permiso no hace nada. */
    fun refreshLocation() {
        viewModelScope.launch {
            val location = getCurrentLocation() ?: return@launch
            val hadLocation = _uiState.value.userLocation != null
            _uiState.update { it.copy(userLocation = location) }
            if (!hadLocation && _uiState.value.filter == StoreFilter.NEARBY) search()
        }
    }

    fun setFilter(filter: StoreFilter) {
        _uiState.update { it.copy(filter = filter, errorMessage = null) }
        search()
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(nameQuery = value, errorMessage = null) }
    }

    fun onAddressChange(value: String) {
        _uiState.update { it.copy(addressQuery = value, errorMessage = null) }
    }

    fun onMinRatingChange(value: String) {
        _uiState.update {
            it.copy(minRatingQuery = value, useAdvancedFilter = true, errorMessage = null)
        }
    }

    fun setMinRatingPreset(value: Double) {
        _uiState.update {
            it.copy(
                minRatingQuery = "%.1f".format(value),
                useAdvancedFilter = true,
                errorMessage = null,
            )
        }
        search()
    }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            val name = _uiState.value.nameQuery.trim().ifEmpty { null }
            val address = _uiState.value.addressQuery.trim().ifEmpty { null }
            val minRating = parseMinRating(_uiState.value.minRatingQuery)
            val useAdvanced = _uiState.value.useAdvancedFilter || minRating != null

            val location = _uiState.value.userLocation
            if (_uiState.value.filter == StoreFilter.NEARBY && location == null) {
                _uiState.update {
                    it.copy(isLoading = false, stores = emptyList(), infoMessage = "Activa la ubicación para ver las ópticas cercanas.")
                }
                return@launch
            }
            val result =
                if (_uiState.value.filter == StoreFilter.NEARBY && location != null) {
                    getNearbyOpticalStores(location).map { nearby ->
                        val found = nearby.filter { name == null || it.name.contains(name, ignoreCase = true) }
                        OpticalStoreSearchResult(found, if (found.isEmpty()) "No hay ópticas a menos de 5 km." else null)
                    }
                } else if (useAdvanced) {
                    searchOpticalStores(name = name, address = address, minRating = minRating)
                } else {
                    getOpticalStores(name = name, address = address).map { OpticalStoreSearchResult(it, null) }
                }
            result
                .onSuccess { found ->
                    _uiState.update {
                        it.copy(
                            stores = found.stores,
                            infoMessage = found.message?.takeIf { found.stores.isEmpty() },
                            isLoading = false,
                        )
                    }
                    loadFirstSlots(found.stores)
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage())
                    }
                }
        }
    }

    /**
     * Guarda la óptica como favorita con `POST /patients/{id}/favorites`. El backend no tiene
     * listado ni borrado de favoritas; un 409 significa que ya estaba guardada.
     */
    fun saveFavorite(storeId: UUID) {
        if (storeId in _uiState.value.favoriteStoreIds) return
        viewModelScope.launch {
            val result = saveFavoriteStore(storeId)
            val error = result.exceptionOrNull()
            if (result.isSuccess || (error as? HttpException)?.code() == 409) {
                _uiState.update {
                    it.copy(favoriteStoreIds = it.favoriteStoreIds + storeId, infoMessage = "Óptica guardada en tus favoritas.")
                }
            } else {
                _uiState.update { it.copy(errorMessage = error?.toUserMessage()) }
            }
        }
    }

    private fun loadFirstSlots(stores: List<OpticalStore>) {
        val missing = stores.map { it.id }.filterNot { it in _uiState.value.firstSlots }
        if (missing.isEmpty()) return
        viewModelScope.launch {
            val slots = getFirstAvailableSlots(missing)
            _uiState.update { it.copy(firstSlots = it.firstSlots + slots) }
        }
    }

    private fun parseMinRating(raw: String): Double? {
        val trimmed = raw.trim().replace(',', '.')
        if (trimmed.isEmpty()) return null
        return trimmed.toDoubleOrNull()
    }
}
