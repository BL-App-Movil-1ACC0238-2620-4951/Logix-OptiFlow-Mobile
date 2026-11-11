package com.logix.optiflow.presentation.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.clinical.Sale
import com.logix.optiflow.domain.model.clinical.SaleStatus
import com.logix.optiflow.domain.usecase.clinical.CreatePaymentLinkUseCase
import com.logix.optiflow.domain.usecase.clinical.GetQuotationUseCase
import com.logix.optiflow.domain.usecase.clinical.GetPatientSalesUseCase
import com.logix.optiflow.domain.usecase.notification.GetLoyaltyAccountUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PurchaseUi(
    val id: String,
    val date: String,
    val amount: String,
    val paymentMethod: String,
    val status: String,
    val receiptNumber: String?,
    /** Pendiente de pago: el paciente puede pagarla con tarjeta o Yape. */
    val payable: Boolean = false,
    /** El último intento con Mercado Pago fue rechazado. */
    val paymentRejected: Boolean = false,
)

data class PatientAccountUiState(
    /** Saldo de puntos; null mientras se consulta o si falló. */
    val points: Int? = null,
    val purchases: List<PurchaseUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** Venta cuyo link de pago se está pidiendo. */
    val payingSaleId: String? = null,
    /** Link de Checkout Pro que la pantalla abre una sola vez. */
    val paymentUrl: String? = null,
    val paymentError: String? = null,
)

/** Puntos de fidelidad y compras del paciente con sesión. */
@HiltViewModel
class PatientAccountViewModel @Inject constructor(
    private val getPatientSession: GetPatientSessionUseCase,
    private val getLoyaltyAccount: GetLoyaltyAccountUseCase,
    private val getPatientSales: GetPatientSalesUseCase,
    private val getQuotation: GetQuotationUseCase,
    private val createPaymentLink: CreatePaymentLinkUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientAccountUiState())
    val uiState: StateFlow<PatientAccountUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val patientId = getPatientSession()?.patientId
            if (patientId == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            val points = async { getLoyaltyAccount(patientId) }
            val sales = async { getPatientSales(patientId) }
            val salesResult = sales.await()
            // Una venta pendiente aún no tiene pago ni boleta: el monto sale del total de su cotización.
            val pendingTotals =
                salesResult.getOrDefault(emptyList())
                    .filter { it.status == SaleStatus.PENDING_PAYMENT && it.quotationId != null }
                    .map { sale -> async { sale.id to getQuotation(sale.quotationId!!).getOrNull()?.total } }
                    .awaitAll()
                    .toMap()
            _uiState.update {
                it.copy(
                    points = points.await().getOrNull()?.balance,
                    purchases = salesResult.getOrDefault(emptyList()).map { sale -> toPurchaseUi(sale, pendingTotals[sale.id]) },
                    errorMessage = salesResult.exceptionOrNull()?.toUserMessage(),
                    isLoading = false,
                )
            }
        }
    }

    /** Pide el link de pago de Mercado Pago (`POST /sales/{id}/payment-preference`). */
    fun pay(saleId: String) {
        if (_uiState.value.payingSaleId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(payingSaleId = saleId, paymentError = null) }
            createPaymentLink(UUID.fromString(saleId))
                .onSuccess { url -> _uiState.update { it.copy(payingSaleId = null, paymentUrl = url) } }
                .onFailure { error -> _uiState.update { it.copy(payingSaleId = null, paymentError = error.toUserMessage()) } }
        }
    }

    fun paymentOpened() = _uiState.update { it.copy(paymentUrl = null) }

    private fun toPurchaseUi(sale: Sale, pendingTotal: Double?): PurchaseUi =
        PurchaseUi(
            id = sale.id.toString(),
            date = sale.date?.let { dateFormat.format(it) } ?: "Sin fecha",
            amount = (sale.amount ?: pendingTotal)?.let { String.format(Locale.US, "S/ %.2f", it) } ?: "—",
            paymentMethod = methodLabel(sale.paymentMethod),
            status = statusLabel(sale.status),
            receiptNumber = sale.receiptNumber,
            payable = sale.status == SaleStatus.PENDING_PAYMENT,
            paymentRejected = sale.status == SaleStatus.PENDING_PAYMENT && sale.paymentStatus == "REJECTED",
        )

    private fun methodLabel(raw: String?): String =
        when (raw?.uppercase(Locale.US)) {
            null -> "Pago pendiente"
            "CASH" -> "Efectivo"
            "CARD" -> "Tarjeta"
            "YAPE" -> "Yape"
            else -> raw.lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) }
        }

    private fun statusLabel(raw: String): String =
        when (raw.uppercase(Locale.US)) {
            "PAID" -> "Pagada"
            "PENDING_PAYMENT" -> "Pendiente de pago"
            "CLOSED" -> "Completada"
            else -> raw.lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) }
        }

    private companion object {
        val dateFormat: DateTimeFormatter =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "PE")).withZone(STORE_ZONE)
    }
}
