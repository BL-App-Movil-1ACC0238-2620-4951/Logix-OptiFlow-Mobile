package com.logix.optiflow.presentation.staff

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.clinical.Quotation
import com.logix.optiflow.domain.model.clinical.Sale
import com.logix.optiflow.domain.model.clinical.SaleStatus
import com.logix.optiflow.domain.usecase.clinical.CloseSaleUseCase
import com.logix.optiflow.domain.usecase.clinical.GetQuotationUseCase
import com.logix.optiflow.domain.usecase.clinical.GetSaleUseCase
import com.logix.optiflow.domain.usecase.clinical.RecordCashPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SaleUiState(
    val isLoading: Boolean = true,
    val sale: Sale? = null,
    val quotation: Quotation? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * Venta del asesor. En efectivo registra el pago (`POST /sales/{id}/payments`) y cierra la venta
 * (`PATCH /sales/{id}/status`) de una vez; con tarjeta o Yape el paciente paga desde su app y el asesor
 * cierra la venta cuando aparece pagada. Al cerrarla el backend emite la boleta, genera la orden de
 * trabajo y descuenta la montura del stock.
 */
@HiltViewModel
class SaleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSale: GetSaleUseCase,
    private val getQuotation: GetQuotationUseCase,
    private val recordCashPayment: RecordCashPaymentUseCase,
    private val closeSale: CloseSaleUseCase,
) : ViewModel() {

    private val saleId: UUID? =
        savedStateHandle.get<String>("saleId")?.let { runCatching { UUID.fromString(it) }.getOrNull() }

    private val _uiState = MutableStateFlow(SaleUiState())
    val uiState: StateFlow<SaleUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val id = saleId ?: run {
            _uiState.update { it.copy(isLoading = false, errorMessage = "No se encontró la venta.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            getSale(id)
                .onSuccess { sale ->
                    val quotation = _uiState.value.quotation ?: sale.quotationId?.let { getQuotation(it).getOrNull() }
                    _uiState.update { it.copy(isLoading = false, sale = sale, quotation = quotation) }
                }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) } }
        }
    }

    /** Cobra el total en efectivo y cierra la venta. Si el cierre falla, la venta queda pagada y se reintenta. */
    fun payCashAndClose() {
        val state = _uiState.value
        val sale = state.sale ?: return
        val total = state.quotation?.total ?: return
        if (state.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val paid = recordCashPayment(sale.id, total).getOrElse { error ->
                _uiState.update { it.copy(isSaving = false, errorMessage = error.toUserMessage()) }
                return@launch
            }
            _uiState.update { it.copy(sale = paid) }
            close(paid.id)
        }
    }

    fun closePaidSale() {
        val sale = _uiState.value.sale ?: return
        if (_uiState.value.isSaving || sale.status != SaleStatus.PAID) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            close(sale.id)
        }
    }

    private suspend fun close(id: UUID) {
        closeSale(id)
            .onSuccess { closed -> _uiState.update { it.copy(isSaving = false, sale = closed) } }
            .onFailure { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.toUserMessage()) } }
    }
}
