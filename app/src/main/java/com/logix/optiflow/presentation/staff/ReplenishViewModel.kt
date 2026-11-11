package com.logix.optiflow.presentation.staff

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.inventory.Supplier
import com.logix.optiflow.domain.usecase.inventory.GetFrameCatalogUseCase
import com.logix.optiflow.domain.usecase.inventory.GetStoreInventoryUseCase
import com.logix.optiflow.domain.usecase.inventory.GetSuppliersUseCase
import com.logix.optiflow.domain.usecase.inventory.ReplenishStockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** El backend acepta de 1 a 1000 unidades por reposición. */
const val MAX_REPLENISHMENT = 1000

data class ReplenishUiState(
    val isLoading: Boolean = true,
    val rows: List<InventoryRow> = emptyList(),
    val suppliers: List<Supplier> = emptyList(),
    val sku: String? = null,
    val supplierId: UUID? = null,
    val quantity: Int = 1,
    /** Nuevo mínimo; vacío conserva el actual (o 5 si la montura es nueva en la óptica). */
    val minimumInput: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val done: Boolean = false,
) {
    val selected: InventoryRow? get() = rows.firstOrNull { it.frame.sku == sku }
    val supplier: Supplier? get() = suppliers.firstOrNull { it.id == supplierId }
}

/** Reposición de stock (`POST /inventory/replenishments`) con el proveedor que entregó las monturas. */
@HiltViewModel
class ReplenishViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCatalog: GetFrameCatalogUseCase,
    private val getInventory: GetStoreInventoryUseCase,
    private val getSuppliers: GetSuppliersUseCase,
    private val replenish: ReplenishStockUseCase,
) : ViewModel() {

    private val initialSku: String? = savedStateHandle.get<String>("sku")?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(ReplenishUiState(sku = initialSku))
    val uiState: StateFlow<ReplenishUiState> = _uiState.asStateFlow()

    private var storeId: UUID? = null

    fun load(storeId: UUID?) {
        if (storeId == null || storeId == this.storeId) return
        this.storeId = storeId
        viewModelScope.launch {
            val stock = async { getInventory(storeId) }
            val suppliers = async { getSuppliers() }
            val catalog = getCatalog()
            val stockBySku = stock.await().getOrNull().orEmpty().associateBy { it.sku }
            val supplierList = suppliers.await().getOrNull().orEmpty()
            catalog
                .onSuccess { frames ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            rows = frames.map { f -> InventoryRow(f, stockBySku[f.sku]) },
                            suppliers = supplierList,
                            sku = it.sku ?: frames.firstOrNull()?.sku,
                            supplierId = supplierList.firstOrNull()?.id,
                            errorMessage = if (supplierList.isEmpty()) "No hay proveedores registrados." else null,
                        )
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) } }
        }
    }

    fun selectSku(sku: String) = _uiState.update { it.copy(sku = sku, errorMessage = null) }

    fun selectSupplier(id: UUID) = _uiState.update { it.copy(supplierId = id, errorMessage = null) }

    fun changeQuantity(delta: Int) = _uiState.update { it.copy(quantity = (it.quantity + delta).coerceIn(1, MAX_REPLENISHMENT)) }

    fun setMinimum(value: String) = _uiState.update { it.copy(minimumInput = value.filter(Char::isDigit).take(4)) }

    fun submit() {
        val state = _uiState.value
        val store = storeId ?: return
        val sku = state.sku
        val supplierId = state.supplierId
        if (sku == null || supplierId == null) {
            _uiState.update { it.copy(errorMessage = "Elige la montura y el proveedor.") }
            return
        }
        if (state.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            replenish(store, sku, supplierId, state.quantity, state.minimumInput.toIntOrNull())
                .onSuccess { _uiState.update { it.copy(isSaving = false, done = true) } }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.toUserMessage()) } }
        }
    }
}
