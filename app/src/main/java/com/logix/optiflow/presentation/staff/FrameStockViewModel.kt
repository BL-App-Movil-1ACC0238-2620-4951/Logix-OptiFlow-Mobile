package com.logix.optiflow.presentation.staff

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.domain.model.inventory.StockMovement
import com.logix.optiflow.domain.usecase.inventory.GetStoreFrameUseCase
import com.logix.optiflow.domain.usecase.inventory.GetStockMovementsUseCase
import com.logix.optiflow.domain.usecase.inventory.UpdateFramePriceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Días hacia atrás que se muestran en el historial de una montura. */
const val MOVEMENT_DAYS = 30L

private val MOVEMENT_TIME: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("es-PE")).withZone(STORE_ZONE)

/** "10 nov 2026, 17:20" en la hora de la óptica. */
fun movementTime(instant: Instant?): String = instant?.let { MOVEMENT_TIME.format(it).replace(".", "") } ?: "—"

fun movementReason(movement: StockMovement): String =
    when (movement.reason) {
        "REPLENISHMENT" -> "Reposición de proveedor"
        "SALE" -> "Venta"
        "ADJUSTMENT" -> "Ajuste"
        else -> if (movement.isEntry) "Entrada" else "Salida"
    }

data class FrameStockUiState(
    val isLoading: Boolean = true,
    val frame: FrameModel? = null,
    val movements: List<StockMovement> = emptyList(),
    val editingPrice: Boolean = false,
    val priceInput: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)

/**
 * Una montura con su stock en la óptica (`GET /inventory/frames/{sku}?opticalStoreId=`), su precio
 * (`PATCH /inventory/frames/{sku}/price`) y, para el admin, sus movimientos de los últimos 30 días.
 */
@HiltViewModel
class FrameStockViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getFrame: GetStoreFrameUseCase,
    private val getMovements: GetStockMovementsUseCase,
    private val updatePrice: UpdateFramePriceUseCase,
) : ViewModel() {

    private val sku: String = savedStateHandle.get<String>("id").orEmpty()

    private val _uiState = MutableStateFlow(FrameStockUiState())
    val uiState: StateFlow<FrameStockUiState> = _uiState.asStateFlow()

    fun load(storeId: UUID?, withMovements: Boolean) {
        if (storeId == null) return
        viewModelScope.launch {
            getFrame(sku, storeId)
                .onSuccess { frame -> _uiState.update { it.copy(isLoading = false, frame = frame) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) } }
            if (withMovements) {
                val today = LocalDate.now(STORE_ZONE)
                getMovements(storeId, today.minusDays(MOVEMENT_DAYS), today, sku)
                    .onSuccess { movements -> _uiState.update { it.copy(movements = movements) } }
            }
        }
    }

    fun editPrice(editing: Boolean) =
        _uiState.update {
            it.copy(
                editingPrice = editing,
                priceInput = if (editing) it.frame?.price?.let { p -> String.format(Locale.US, "%.2f", p) }.orEmpty() else "",
                errorMessage = null,
                message = null,
            )
        }

    fun setPrice(value: String) = _uiState.update { it.copy(priceInput = value.filter { c -> c.isDigit() || c == '.' }.take(11)) }

    fun savePrice() {
        val state = _uiState.value
        val price = state.priceInput.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Escribe un precio mayor que cero.") }
            return
        }
        if (state.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            updatePrice(sku, price)
                .onSuccess { updated ->
                    _uiState.update {
                        // El PATCH no trae el stock de la óptica; se conserva el que ya se mostraba.
                        val frame = updated.copy(quantity = it.frame?.quantity, minimumStock = it.frame?.minimumStock)
                        it.copy(isSaving = false, frame = frame, editingPrice = false, message = "Precio actualizado a ${soles(frame.price)}.")
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.toUserMessage()) } }
        }
    }
}
