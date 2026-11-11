package com.logix.optiflow.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.domain.model.searchbooking.OpticalStoreSearchResult
import com.logix.optiflow.domain.usecase.searchbooking.GetOpticalStoresUseCase
import com.logix.optiflow.domain.usecase.searchbooking.SaveFavoriteStoreUseCase
import com.logix.optiflow.domain.usecase.searchbooking.SearchOpticalStoresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

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
)

@HiltViewModel
class StoreSearchViewModel @Inject constructor(
    private val getOpticalStores: GetOpticalStoresUseCase,
    private val searchOpticalStores: SearchOpticalStoresUseCase,
    private val saveFavoriteStore: SaveFavoriteStoreUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreSearchUiState())
    val uiState: StateFlow<StoreSearchUiState> = _uiState.asStateFlow()

    init {
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

            val result =
                if (useAdvanced) {
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

    private fun parseMinRating(raw: String): Double? {
        val trimmed = raw.trim().replace(',', '.')
        if (trimmed.isEmpty()) return null
        return trimmed.toDoubleOrNull()
    }
}
