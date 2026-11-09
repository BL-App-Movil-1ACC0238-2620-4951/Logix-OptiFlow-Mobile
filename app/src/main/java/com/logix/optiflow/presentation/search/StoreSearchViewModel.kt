package com.logix.optiflow.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.domain.usecase.searchbooking.GetOpticalStoresUseCase
import com.logix.optiflow.domain.usecase.searchbooking.SearchOpticalStoresUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoreSearchUiState(
    val nameQuery: String = "",
    val addressQuery: String = "",
    val minRatingQuery: String = "",
    val useAdvancedFilter: Boolean = false,
    val stores: List<OpticalStore> = emptyList(),
    val infoMessage: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class StoreSearchViewModel(
    private val getOpticalStores: GetOpticalStoresUseCase,
    private val searchOpticalStores: SearchOpticalStoresUseCase,
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
            try {
                val name = _uiState.value.nameQuery.trim().ifEmpty { null }
                val address = _uiState.value.addressQuery.trim().ifEmpty { null }
                val minRating = parseMinRating(_uiState.value.minRatingQuery)
                val useAdvanced = _uiState.value.useAdvancedFilter || minRating != null

                if (useAdvanced) {
                    val result = searchOpticalStores(name = name, address = address, minRating = minRating)
                    _uiState.update {
                        it.copy(
                            stores = result.stores,
                            infoMessage = result.message?.takeIf { msg -> result.stores.isEmpty() },
                            isLoading = false,
                        )
                    }
                } else {
                    val stores = getOpticalStores(name = name, address = address)
                    _uiState.update { it.copy(stores = stores, isLoading = false) }
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.toUserMessage())
                }
            }
        }
    }

    private fun parseMinRating(raw: String): Double? {
        val trimmed = raw.trim().replace(',', '.')
        if (trimmed.isEmpty()) return null
        return trimmed.toDoubleOrNull()
    }
}
