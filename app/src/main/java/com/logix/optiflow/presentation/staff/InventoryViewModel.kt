package com.logix.optiflow.presentation.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.demo.ProductionOrder
import com.logix.optiflow.data.demo.ProductionStage
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.domain.model.inventory.StockItem
import com.logix.optiflow.domain.usecase.inventory.GetFrameCatalogUseCase
import com.logix.optiflow.domain.usecase.inventory.GetLowStockAlertsUseCase
import com.logix.optiflow.domain.usecase.inventory.GetStoreInventoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Alertas de producción: una por orden atrasada y una si hay pedidos listos para entregar. */
fun productionAlertCount(orders: List<ProductionOrder>): Int {
    val open = orders.filter { it.stage != ProductionStage.DELIVERED }
    return open.count { it.needsAttention && it.id != null } + if (open.any { it.stage == ProductionStage.READY_FOR_DELIVERY }) 1 else 0
}

/** Una montura del catálogo con sus unidades en la óptica (null si nunca se repuso ahí). */
data class InventoryRow(val frame: FrameModel, val stock: StockItem?)

data class InventoryUiState(
    val isLoading: Boolean = true,
    val rows: List<InventoryRow> = emptyList(),
    val query: String = "",
    val alerts: List<StockItem> = emptyList(),
    val errorMessage: String? = null,
) {
    val filtered: List<InventoryRow>
        get() {
            val q = query.trim().lowercase(Locale.ROOT)
            if (q.isEmpty()) return rows
            return rows.filter { row ->
                listOfNotNull(row.frame.name, row.frame.sku, row.frame.color, row.frame.material)
                    .any { it.lowercase(Locale.ROOT).contains(q) }
            }
        }
    val totalUnits: Int get() = rows.sumOf { it.stock?.quantity ?: 0 }
    val lowCount: Int get() = rows.count { it.stock?.low == true }
}

/**
 * Catálogo de monturas (`GET /inventory/frames`) con el stock de la óptica del usuario
 * (`GET /inventory/stock`); el admin también ve las alertas de stock crítico (`GET /inventory/alerts`).
 */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val getCatalog: GetFrameCatalogUseCase,
    private val getInventory: GetStoreInventoryUseCase,
    private val getAlerts: GetLowStockAlertsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    fun setQuery(value: String) = _uiState.update { it.copy(query = value.take(60)) }

    fun load(storeId: UUID?) {
        if (storeId == null) return
        viewModelScope.launch {
            val stock = async { getInventory(storeId) }
            val catalog = getCatalog()
            val stockBySku = stock.await().getOrNull().orEmpty().associateBy { it.sku }
            catalog
                .onSuccess { frames ->
                    _uiState.update {
                        it.copy(isLoading = false, rows = frames.map { f -> InventoryRow(f, stockBySku[f.sku]) }, errorMessage = null)
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) } }
        }
    }

    fun loadAlerts(storeId: UUID?) {
        if (storeId == null) return
        viewModelScope.launch {
            getAlerts(storeId)
                .onSuccess { alerts -> _uiState.update { it.copy(isLoading = false, alerts = alerts, errorMessage = null) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) } }
        }
    }
}
