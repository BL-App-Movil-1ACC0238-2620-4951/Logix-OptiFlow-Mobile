package com.logix.optiflow.presentation.staff

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.production.NamedRef
import com.logix.optiflow.domain.model.production.WorkOrder
import com.logix.optiflow.domain.model.production.WorkOrderStatus
import com.logix.optiflow.domain.usecase.production.AssignTechnicianUseCase
import com.logix.optiflow.domain.usecase.production.CompleteLensesUseCase
import com.logix.optiflow.domain.usecase.production.GetWorkOrderUseCase
import com.logix.optiflow.domain.usecase.production.GetWorkshopCatalogUseCase
import com.logix.optiflow.domain.usecase.production.NotifyDeliveryDelayUseCase
import com.logix.optiflow.domain.usecase.production.SendToLaboratoryUseCase
import com.logix.optiflow.domain.usecase.production.UpdateWorkOrderStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
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

/** Nombre en español de cada etapa de la orden. */
fun workOrderStatusLabel(status: String): String =
    when (status) {
        WorkOrderStatus.PENDING -> "Pendiente"
        WorkOrderStatus.IN_WORKSHOP -> "En taller"
        WorkOrderStatus.QUALITY_CONTROL -> "Control de calidad"
        WorkOrderStatus.READY_FOR_DELIVERY -> "Lista para entrega"
        WorkOrderStatus.DELIVERED -> "Entregada"
        else -> status
    }

/** "17 nov 2026" a partir de una fecha ISO del backend. */
fun workOrderDate(iso: String?): String? =
    iso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-PE")))
        ?.replace(".", "")

data class WorkOrderUiState(
    val isLoading: Boolean = true,
    val order: WorkOrder? = null,
    val technicians: List<NamedRef> = emptyList(),
    val laboratories: List<NamedRef> = emptyList(),
    val showDelay: Boolean = false,
    val delayReason: String = "",
    /** Nueva fecha en dd/mm/aaaa. */
    val delayDate: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null,
)

/**
 * Seguimiento de una orden de trabajo en el taller (`GET /work-orders/{id}`): el técnico la asigna,
 * la envía al laboratorio, marca los lentes, la avanza por el Kanban hasta la entrega y reporta
 * retrasos. Los demás roles solo la leen.
 */
@HiltViewModel
class WorkOrderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWorkOrder: GetWorkOrderUseCase,
    private val getWorkshopCatalog: GetWorkshopCatalogUseCase,
    private val assignTechnician: AssignTechnicianUseCase,
    private val sendToLaboratory: SendToLaboratoryUseCase,
    private val updateStatus: UpdateWorkOrderStatusUseCase,
    private val completeLenses: CompleteLensesUseCase,
    private val notifyDelay: NotifyDeliveryDelayUseCase,
) : ViewModel() {

    private val orderId: UUID? =
        savedStateHandle.get<String>("id")?.let { runCatching { UUID.fromString(it) }.getOrNull() }

    private val _uiState = MutableStateFlow(WorkOrderUiState())
    val uiState: StateFlow<WorkOrderUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val id = orderId ?: run {
            _uiState.update { it.copy(isLoading = false, errorMessage = "No se encontró la orden.") }
            return
        }
        viewModelScope.launch {
            val catalog = async { getWorkshopCatalog() }
            getWorkOrder(id)
                .onSuccess { order -> _uiState.update { it.copy(isLoading = false, order = order) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) } }
            val loaded = catalog.await()
            _uiState.update { it.copy(technicians = loaded.technicians, laboratories = loaded.laboratories) }
        }
    }

    fun assign(technicianId: UUID) = run("Técnico asignado.") { assignTechnician(it, technicianId) }

    fun sendTo(laboratoryId: UUID) = run("Orden enviada al laboratorio.") { sendToLaboratory(it, laboratoryId) }

    fun completeAllLenses() = run("Lentes marcados como fabricados.") { completeLenses(it) }

    fun completeLens(lensId: UUID) = run("Lente marcado como fabricado.") { completeLenses(it, listOf(lensId)) }

    /** Avanza a la siguiente etapa del Kanban; en READY_FOR_DELIVERY registra la entrega. */
    fun advance() {
        val next = _uiState.value.order?.status?.let(WorkOrderStatus::next) ?: return
        val done = if (next == WorkOrderStatus.DELIVERED) "Entrega registrada." else "Orden en ${workOrderStatusLabel(next).lowercase()}."
        run(done) { updateStatus(it, next) }
    }

    fun showDelay(show: Boolean) = _uiState.update { it.copy(showDelay = show, delayReason = "", delayDate = "", errorMessage = null) }

    fun setDelayReason(value: String) = _uiState.update { it.copy(delayReason = value.take(255)) }

    fun setDelayDate(value: String) = _uiState.update { it.copy(delayDate = value.filter { c -> c.isDigit() || c == '/' }.take(10)) }

    fun reportDelay() {
        val state = _uiState.value
        val reason = state.delayReason.trim()
        if (reason.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Escribe el motivo del retraso.") }
            return
        }
        val date =
            runCatching { LocalDate.parse(state.delayDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrNull()
        val current = state.order?.estimatedDeliveryDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (date == null) {
            _uiState.update { it.copy(errorMessage = "Escribe la nueva fecha como dd/mm/aaaa.") }
            return
        }
        if (date.isBefore(LocalDate.now(STORE_ZONE)) || (current != null && !date.isAfter(current))) {
            _uiState.update { it.copy(errorMessage = "La nueva fecha debe ser posterior a la entrega actual.") }
            return
        }
        run("Retraso reportado; se avisó al paciente.", closeDelay = true) { notifyDelay(it, reason, date.toString()) }
    }

    private fun run(done: String, closeDelay: Boolean = false, call: suspend (UUID) -> Result<WorkOrder>) {
        val id = orderId ?: return
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, message = null) }
            call(id)
                .onSuccess { order ->
                    _uiState.update {
                        it.copy(isSaving = false, order = order, message = done, showDelay = if (closeDelay) false else it.showDelay)
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.toUserMessage()) } }
        }
    }
}
