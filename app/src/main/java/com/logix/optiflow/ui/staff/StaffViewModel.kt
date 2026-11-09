package com.logix.optiflow.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.demo.AgendaItem
import com.logix.optiflow.data.demo.AgendaStatus
import com.logix.optiflow.data.demo.DemoData
import com.logix.optiflow.data.demo.ProductionOrder
import com.logix.optiflow.data.demo.ProductionStage
import com.logix.optiflow.data.demo.StaffPatient
import com.logix.optiflow.data.remote.api.SearchBookingApi
import com.logix.optiflow.data.remote.dto.AppointmentDto
import com.logix.optiflow.data.remote.dto.WorkOrderDto
import com.logix.optiflow.domain.usecase.GetPatientSessionUseCase
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StaffUiState(
    val patients: List<StaffPatient> = DemoData.staffPatients,
    val agenda: List<AgendaItem> = DemoData.agenda,
    val agendaFromApi: Boolean = false,
    val productionOrders: List<ProductionOrder> = DemoData.productionOrders,
    val productionFromApi: Boolean = false,
    val stock: Map<String, Int> = DemoData.products.associate { it.id to it.available },
    val clinicalStage: Int = 1,
    val workOrderSent: Boolean = false,
)

/**
 * Estado del personal clínico. Las órdenes de trabajo vienen de `GET /work-orders` y la agenda del
 * día de `GET /appointments` de la óptica del usuario; pacientes e inventario usan datos de
 * demostración.
 */
class StaffViewModel(
    private val api: SearchBookingApi,
    private val getSession: GetPatientSessionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffUiState())
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    init {
        refreshProduction()
        refreshAgenda()
    }

    fun refreshAgenda() {
        viewModelScope.launch {
            val items = runCatching { loadTodayAgenda() }.getOrDefault(emptyList())
            if (items.isNotEmpty()) {
                _uiState.update { it.copy(agenda = items, agendaFromApi = true) }
            }
        }
    }

    private suspend fun loadTodayAgenda(): List<AgendaItem> {
        val session = getSession() ?: return emptyList()
        if (session.isPatient) return emptyList()
        val storeId = api.getStaffMember(session.patientId).opticalStoreId
        val now = Instant.now()
        return api.getStoreAgenda(storeId, LocalDate.now().toString())
            .filter { it.status != "CANCELLED" }
            .map { appointment ->
                val name = runCatching { api.getPatient(appointment.patientId).name }.getOrDefault("Paciente")
                toAgendaItem(appointment, name, now)
            }
    }

    /**
     * Una cita confirmada que empieza en los próximos [WAITING_WINDOW_MINUTES] minutos (o ya empezó)
     * se muestra en la sala de espera; el backend no tiene un estado aparte para eso.
     */
    private fun toAgendaItem(dto: AppointmentDto, name: String, now: Instant): AgendaItem {
        val inWaitingWindow =
            !now.isBefore(dto.startDateTime.minus(Duration.ofMinutes(WAITING_WINDOW_MINUTES))) &&
                now.isBefore(dto.endDateTime)
        val status =
            when (dto.status) {
                "COMPLETED" -> AgendaStatus.DONE
                "PENDING" -> AgendaStatus.PENDING
                else -> if (inWaitingWindow) AgendaStatus.WAITING else AgendaStatus.CONFIRMED
            }
        val minutes = Duration.between(dto.startDateTime, dto.endDateTime).toMinutes()
        return AgendaItem(
            time = TIME_FORMAT.format(dto.startDateTime),
            duration = "$minutes min",
            patientId = dto.patientId.toString(),
            name = name,
            service = "Evaluación visual",
            status = status,
        )
    }

    fun refreshProduction() {
        viewModelScope.launch {
            val orders = runCatching { api.getWorkOrders() }.getOrDefault(emptyList())
            if (orders.isNotEmpty()) {
                _uiState.update {
                    it.copy(productionOrders = orders.map(::toProductionOrder), productionFromApi = true)
                }
            }
        }
    }

    fun addPatient(name: String, phone: String, rut: String): StaffPatient {
        val clean = name.trim().ifBlank { "Paciente nuevo" }
        val initials = clean.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
        val patient =
            StaffPatient(
                id = "new-${System.currentTimeMillis()}",
                initials = initials,
                name = clean.split(' ').take(2).joinToString(" "),
                fullName = clean,
                rut = rut.ifBlank { "—" },
                note = "Nuevo paciente",
                phone = phone.ifBlank { "—" },
                lastVisit = "—",
                ficha = "OPT-2024-" + (900..999).random(),
                age = 30,
            )
        _uiState.update { it.copy(patients = listOf(patient) + it.patients) }
        return patient
    }

    fun adjustStock(productId: String, delta: Int) {
        _uiState.update { state ->
            val current = state.stock[productId] ?: 0
            state.copy(stock = state.stock + (productId to (current + delta).coerceAtLeast(0)))
        }
    }

    fun setClinicalStage(stage: Int) = _uiState.update { it.copy(clinicalStage = stage) }

    fun markWorkOrderSent() = _uiState.update { it.copy(workOrderSent = true) }

    private companion object {
        const val WAITING_WINDOW_MINUTES = 15L
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
    }

    private fun toProductionOrder(dto: WorkOrderDto): ProductionOrder {
        val stage =
            runCatching { ProductionStage.valueOf(dto.status.uppercase(Locale.US)) }
                .getOrDefault(ProductionStage.PENDING)
        val due =
            dto.estimatedDeliveryDate
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?.let { date ->
                    when {
                        dto.delayed -> "Atrasada"
                        date == LocalDate.now() -> "Entrega hoy"
                        else -> "Entrega " + date.format(DateTimeFormatter.ofPattern("d MMM", Locale("es", "PE"))).replace(".", "")
                    }
                } ?: "Sin fecha"
        val patientName =
            dto.patientId?.let { id -> DemoData.staffPatients.getOrNull(id.hashCode().mod(DemoData.staffPatients.size))?.name }
                ?: "Paciente"
        return ProductionOrder(
            code = "OT-" + dto.id.toString().take(4).uppercase(Locale.US),
            patient = patientName,
            detail = dto.lenses?.firstOrNull()?.specifications ?: "Monofocal 1.60",
            stage = stage,
            due = due,
            needsAttention = dto.delayed,
        )
    }
}
