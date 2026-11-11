package com.logix.optiflow.presentation.staff

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.clinical.Quotation
import com.logix.optiflow.domain.model.clinical.QuotationItem
import com.logix.optiflow.domain.model.clinical.QuotationStatus
import com.logix.optiflow.domain.model.clinical.Sale
import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.domain.usecase.clinical.ApplyQuotationDiscountUseCase
import com.logix.optiflow.domain.usecase.clinical.GenerateQuotationUseCase
import com.logix.optiflow.domain.usecase.clinical.GetPatientClinicalRecordsUseCase
import com.logix.optiflow.domain.usecase.clinical.GetPatientSalesUseCase
import com.logix.optiflow.domain.usecase.clinical.GetRecordQuotationsUseCase
import com.logix.optiflow.domain.usecase.clinical.RegisterSaleUseCase
import com.logix.optiflow.domain.usecase.clinical.UpdateQuotationStatusUseCase
import com.logix.optiflow.domain.usecase.inventory.GetFrameCatalogUseCase
import com.logix.optiflow.domain.usecase.inventory.GetStoreStockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Opción de lente o tratamiento con su precio sugerido. El backend no tiene catálogo de lentes. */
data class QuoteOption(val name: String, val price: Double)

val LENS_OPTIONS =
    listOf(
        QuoteOption("Monofocal 1.56", 120.0),
        QuoteOption("Monofocal 1.60 delgado", 180.0),
        QuoteOption("Bifocal", 250.0),
        QuoteOption("Progresivo", 420.0),
    )

val TREATMENT_OPTIONS =
    listOf(
        QuoteOption("Antirreflejo", 60.0),
        QuoteOption("Filtro de luz azul", 80.0),
        QuoteOption("Fotocromático", 150.0),
    )

fun soles(amount: Double): String = String.format(Locale.US, "S/ %.2f", amount)

data class QuoteUiState(
    val isLoading: Boolean = true,
    /** Motivo por el que no se puede cotizar (sin receta, error al cargar). */
    val blockedMessage: String? = null,
    val clinicalRecordId: UUID? = null,
    val prescriptionLabel: String = "",
    val frames: List<FrameModel> = emptyList(),
    /** Unidades por SKU en la óptica del asesor; vacío si no se pudo consultar. */
    val stock: Map<String, Int> = emptyMap(),
    val frameSku: String? = null,
    val lensIndex: Int? = null,
    val lensPrice: String = "",
    val treatments: Set<String> = emptySet(),
    val quotation: Quotation? = null,
    /** Venta ya registrada para la cotización aprobada. */
    val sale: Sale? = null,
    /** Venta recién registrada: la pantalla navega a ella una sola vez. */
    val openSaleId: UUID? = null,
    /** La última cotización de la ficha fue rechazada: se muestra el aviso y se arma otra. */
    val lastRejectedReason: String? = null,
    val discountIsPercentage: Boolean = true,
    val discountValue: String = "",
    val discountReason: String = "",
    val showReject: Boolean = false,
    val rejectReason: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val selectedFrame: FrameModel? get() = frames.firstOrNull { it.sku == frameSku }

    /** Ítems que se envían al backend con la montura, el lente y los tratamientos elegidos. */
    val draftItems: List<QuotationItem>
        get() =
            buildList {
                selectedFrame?.let { add(QuotationItem("FRAME", it.sku, "Montura ${it.name}", it.price)) }
                lensIndex?.let { index ->
                    val price = lensPrice.replace(',', '.').toDoubleOrNull()
                    if (price != null) add(QuotationItem("LENS", null, "Lentes ${LENS_OPTIONS[index].name}", price))
                }
                TREATMENT_OPTIONS.filter { it.name in treatments }
                    .forEach { add(QuotationItem("TREATMENT", null, it.name, it.price)) }
            }
}

/**
 * Cotización del asesor de ventas. Usa la última ficha del paciente que ya tiene receta: si esa ficha
 * tiene una cotización en borrador o aprobada se retoma (`GET /clinical-records/{id}/quotations`);
 * si no, se arma una nueva con una montura real del inventario (`GET /inventory/frames`).
 */
@HiltViewModel
class QuoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPatientRecords: GetPatientClinicalRecordsUseCase,
    private val getFrameCatalog: GetFrameCatalogUseCase,
    private val getRecordQuotations: GetRecordQuotationsUseCase,
    private val generateQuotation: GenerateQuotationUseCase,
    private val applyDiscount: ApplyQuotationDiscountUseCase,
    private val updateStatus: UpdateQuotationStatusUseCase,
    private val getStoreStock: GetStoreStockUseCase,
    private val getPatientSales: GetPatientSalesUseCase,
    private val registerSale: RegisterSaleUseCase,
) : ViewModel() {

    private val patientId: UUID? =
        savedStateHandle.get<String>("patientId")?.let { runCatching { UUID.fromString(it) }.getOrNull() }

    private val _uiState = MutableStateFlow(QuoteUiState())
    val uiState: StateFlow<QuoteUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val id = patientId ?: run {
            _uiState.update { it.copy(isLoading = false, blockedMessage = "No se encontró al paciente.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, blockedMessage = null, errorMessage = null) }
        viewModelScope.launch {
            val framesCall = async { getFrameCatalog() }
            val recordsResult = getPatientRecords(id)
            val records = recordsResult.getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, blockedMessage = error.toUserMessage()) }
                return@launch
            }
            val record =
                records.filter { it.prescription != null }.maxByOrNull { it.examinationDate ?: Instant.EPOCH }
            if (record == null) {
                _uiState.update {
                    it.copy(isLoading = false, blockedMessage = "El paciente todavía no tiene receta. Primero debe pasar por la atención clínica.")
                }
                return@launch
            }
            val salesCall = async { getPatientSales(id).getOrDefault(emptyList()) }
            val quotations = getRecordQuotations(record.id).getOrDefault(emptyList())
            val frames = framesCall.await().getOrDefault(emptyList())
            val current =
                quotations.lastOrNull { it.status == QuotationStatus.DRAFT }
                    ?: quotations.lastOrNull { it.status == QuotationStatus.APPROVED }
            val rejected = quotations.lastOrNull { it.status == QuotationStatus.REJECTED }
            val prescription = record.prescription
            val date = record.examinationDate?.let { DATE.format(it.atZone(STORE_ZONE)) }
            val suggested = prescription?.treatment.orEmpty()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    clinicalRecordId = record.id,
                    prescriptionLabel =
                        listOfNotNull(
                            date?.let { d -> "Receta del $d" } ?: "Receta vigente",
                            suggested.takeIf { s -> s.isNotBlank() },
                        ).joinToString(" · "),
                    frames = frames,
                    quotation = current,
                    sale = current?.let { q -> salesCall.await().firstOrNull { s -> s.quotationId == q.id } },
                    lastRejectedReason = if (current == null) rejected?.rejectionReason else null,
                    treatments = TREATMENT_OPTIONS.filter { o -> suggested.contains(o.name, ignoreCase = true) }.map { o -> o.name }.toSet(),
                )
            }
        }
    }

    private var stockStoreId: UUID? = null

    /** Stock de la óptica del asesor (`GET /inventory/stock`); se consulta una vez. */
    fun loadStock(storeId: UUID?) {
        if (storeId == null || storeId == stockStoreId) return
        stockStoreId = storeId
        viewModelScope.launch {
            getStoreStock(storeId).onSuccess { stock -> _uiState.update { it.copy(stock = stock) } }
        }
    }

    fun selectFrame(sku: String) {
        if (_uiState.value.stock[sku] == 0) {
            _uiState.update { it.copy(errorMessage = "Esa montura no tiene stock en esta óptica.") }
            return
        }
        _uiState.update { it.copy(frameSku = if (it.frameSku == sku) null else sku, errorMessage = null) }
    }

    /** Registra la venta de la cotización aprobada (`POST /sales`), o abre la que ya existe. */
    fun startSale() {
        val state = _uiState.value
        state.sale?.let { sale -> _uiState.update { it.copy(openSaleId = sale.id) }; return }
        val quotation = state.quotation ?: return
        if (state.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            registerSale(quotation.id)
                .onSuccess { sale -> _uiState.update { it.copy(isSaving = false, sale = sale, openSaleId = sale.id) } }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.toUserMessage()) } }
        }
    }

    fun saleOpened() = _uiState.update { it.copy(openSaleId = null) }

    fun selectLens(index: Int) =
        _uiState.update {
            if (it.lensIndex == index) {
                it.copy(lensIndex = null, lensPrice = "")
            } else {
                it.copy(lensIndex = index, lensPrice = String.format(Locale.US, "%.2f", LENS_OPTIONS[index].price), errorMessage = null)
            }
        }

    fun setLensPrice(value: String) = _uiState.update { it.copy(lensPrice = value.filter { c -> c.isDigit() || c == '.' || c == ',' }) }

    fun toggleTreatment(name: String) =
        _uiState.update { it.copy(treatments = if (name in it.treatments) it.treatments - name else it.treatments + name) }

    fun generate() {
        val state = _uiState.value
        val recordId = state.clinicalRecordId ?: return
        if (state.lensIndex != null && state.lensPrice.replace(',', '.').toDoubleOrNull() == null) {
            _uiState.update { it.copy(errorMessage = "Escribe un precio válido para los lentes.") }
            return
        }
        val items = state.draftItems
        if (items.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Elige al menos una montura o un lente.") }
            return
        }
        save { generateQuotation(recordId, items) }
    }

    fun setDiscountType(percentage: Boolean) = _uiState.update { it.copy(discountIsPercentage = percentage, errorMessage = null) }

    fun setDiscountValue(value: String) = _uiState.update { it.copy(discountValue = value.filter { c -> c.isDigit() || c == '.' || c == ',' }) }

    fun setDiscountReason(value: String) = _uiState.update { it.copy(discountReason = value.take(255)) }

    fun applyDiscount() {
        val state = _uiState.value
        val quotation = state.quotation ?: return
        val value = state.discountValue.replace(',', '.').toDoubleOrNull()
        if (value == null || value <= 0) {
            _uiState.update { it.copy(errorMessage = "Escribe un descuento mayor que cero.") }
            return
        }
        if (state.discountIsPercentage && value > 100) {
            _uiState.update { it.copy(errorMessage = "El descuento no puede pasar del 100 %.") }
            return
        }
        val type = if (state.discountIsPercentage) "PERCENTAGE" else "FIXED_AMOUNT"
        save(clearDiscount = true) {
            applyDiscount(quotation.id, type, value, state.discountReason.trim().ifBlank { null })
        }
    }

    fun approve() {
        val quotation = _uiState.value.quotation ?: return
        save { updateStatus(quotation.id, QuotationStatus.APPROVED) }
    }

    fun showReject(show: Boolean) = _uiState.update { it.copy(showReject = show, rejectReason = "", errorMessage = null) }

    fun setRejectReason(value: String) = _uiState.update { it.copy(rejectReason = value.take(255)) }

    fun reject() {
        val state = _uiState.value
        val quotation = state.quotation ?: return
        val reason = state.rejectReason.trim()
        if (reason.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Escribe el motivo del rechazo.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            updateStatus(quotation.id, QuotationStatus.REJECTED, reason)
                .onSuccess { rejected ->
                    // Se vuelve a armar una cotización nueva con el aviso del rechazo.
                    _uiState.update {
                        it.copy(isSaving = false, quotation = null, showReject = false, lastRejectedReason = rejected.rejectionReason ?: reason)
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.toUserMessage()) } }
        }
    }

    private fun save(clearDiscount: Boolean = false, call: suspend () -> Result<Quotation>) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            call()
                .onSuccess { quotation ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            quotation = quotation,
                            lastRejectedReason = null,
                            discountValue = if (clearDiscount) "" else it.discountValue,
                            discountReason = if (clearDiscount) "" else it.discountReason,
                        )
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.toUserMessage()) } }
        }
    }

    private companion object {
        val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-PE"))
    }
}
