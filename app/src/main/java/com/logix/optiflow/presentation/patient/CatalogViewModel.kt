package com.logix.optiflow.presentation.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.domain.model.inventory.FrameStock
import com.logix.optiflow.domain.usecase.inventory.GetFrameCatalogUseCase
import com.logix.optiflow.domain.usecase.inventory.GetFrameStockUseCase
import com.logix.optiflow.domain.usecase.inventory.ObserveFavoriteFramesUseCase
import com.logix.optiflow.domain.usecase.inventory.ToggleFavoriteFrameUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val frames: List<FrameModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** Stock por óptica de la montura abierta; null mientras se consulta. */
    val stock: List<FrameStock>? = null,
    val favorites: Set<String> = emptySet(),
) {
    val shapes: List<String> get() = frames.mapNotNull { it.shape }.distinct()

    fun frame(sku: String): FrameModel? = frames.firstOrNull { it.sku == sku }
}

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val getFrameCatalog: GetFrameCatalogUseCase,
    private val getFrameStock: GetFrameStockUseCase,
    private val getPatientSession: GetPatientSessionUseCase,
    private val observeFavoriteFrames: ObserveFavoriteFramesUseCase,
    private val toggleFavoriteFrame: ToggleFavoriteFrameUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var stockSku: String? = null
    private var patientId: UUID? = null

    init {
        loadCatalog()
        viewModelScope.launch {
            val id = getPatientSession()?.patientId ?: return@launch
            patientId = id
            observeFavoriteFrames(id).collect { favorites -> _uiState.update { it.copy(favorites = favorites) } }
        }
    }

    fun toggleFavorite(sku: String) {
        val id = patientId ?: return
        viewModelScope.launch { toggleFavoriteFrame(id, sku) }
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getFrameCatalog()
                .onSuccess { frames -> _uiState.update { it.copy(frames = frames, isLoading = false) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) } }
        }
    }

    fun loadStock(sku: String) {
        if (stockSku == sku) return
        stockSku = sku
        _uiState.update { it.copy(stock = null) }
        viewModelScope.launch {
            val stock = getFrameStock(sku).getOrDefault(emptyList())
            _uiState.update { it.copy(stock = stock) }
        }
    }
}
