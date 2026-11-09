package com.logix.optiflow.presentation.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.dto.clinical.ClinicalRecordDto
import com.logix.optiflow.data.remote.dto.production.WorkOrderDto
import com.logix.optiflow.data.remote.dto.searchbooking.BookAppointmentRequest
import com.logix.optiflow.data.remote.dto.searchbooking.TimeSlotDto
import com.logix.optiflow.data.remote.dto.searchbooking.UpdateAppointmentStatusRequest
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.domain.model.searchbooking.PatientSession
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import com.logix.optiflow.domain.usecase.searchbooking.GetOpticalStoresUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientAppointmentsUseCase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val es = Locale("es", "PE")
private val zone: ZoneId = ZoneId.systemDefault()

enum class AppointmentStatus(val label: String) {
    CONFIRMED("Confirmada"),
    PENDING("Pendiente"),
    CANCELLED("Cancelada"),
}

data class AppointmentUi(
    val id: String,
    val day: String,
    val month: String,
    val monthName: String,
    val weekday: String,
    val time: String,
    val endTime: String,
    val title: String,
    val storeName: String,
    val storeAddress: String,
    val professional: String,
    val status: AppointmentStatus,
    val isPast: Boolean,
    val fromApi: Boolean,
    val storeId: UUID? = null,
)

data class OrderUi(
    val id: String,
    val code: String,
    val title: String,
    val dateLabel: String,
    /** 0 recibido · 1 pago · 2 producción · 3 listo · 4 entregado */
    val step: Int,
    val estimate: String,
    val product: String,
    val productDetail: String,
    val pickup: String,
    val history: List<String>,
    val fromApi: Boolean,
) {
    val delivered: Boolean get() = step >= 4
    val statusLabel: String
        get() =
            when (step) {
                0 -> "Pedido recibido"
                1 -> "Pago confirmado"
                2 -> "En producción"
                3 -> "Listo para retirar"
                else -> "Entregado"
            }
}

data class HistoryEntryUi(
    val date: String,
    val title: String,
    val summary: String,
    val professional: String,
)

data class PrescriptionUi(
    val patientName: String,
    val date: String,
    val issuedLabel: String,
    val sphereOD: String,
    val cylinderOD: String,
    val axisOD: String,
    val additionOD: String,
    val sphereOI: String,
    val cylinderOI: String,
    val axisOI: String,
    val additionOI: String,
    val pupillaryDistance: String,
    val diagnoses: List<String>,
    val observations: String,
)

data class PatientUiState(
    val session: PatientSession? = null,
    val appointments: List<AppointmentUi> = emptyList(),
    val orders: List<OrderUi> = emptyList(),
    val history: List<HistoryEntryUi> = emptyList(),
    val prescription: PrescriptionUi = PatientViewModel.demoPrescription("Valentina Silva"),
    val stores: List<OpticalStore> = emptyList(),
    val isLoading: Boolean = true,
) {
    val displayName: String get() = session?.name?.takeIf { it.isNotBlank() } ?: "Valentina Silva"
    val firstName: String get() = displayName.trim().substringBefore(' ')
    val email: String get() = session?.email?.takeIf { it.isNotBlank() } ?: "valentina.silva@email.com"
    val initials: String
        get() =
            displayName.split(' ').filter { it.isNotBlank() }.take(2)
                .joinToString("") { it.first().uppercase() }
    val upcoming: List<AppointmentUi> get() = appointments.filter { !it.isPast && it.status != AppointmentStatus.CANCELLED }
    val nextAppointment: AppointmentUi? get() = upcoming.firstOrNull()
}

/**
 * Estado compartido del paciente (citas, pedidos, historial y receta).
 * Usa la API cuando hay sesión; si el backend aún no tiene datos, muestra el contenido del prototipo.
 */
class PatientViewModel(
    private val patientRepository: PatientRepository,
    private val getPatientAppointments: GetPatientAppointmentsUseCase,
    private val getOpticalStores: GetOpticalStoresUseCase,
    private val api: SearchBookingApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientUiState())
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    private val cancelledIds = mutableSetOf<String>()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val session = runCatching { patientRepository.getSession() }.getOrNull()
            val storesJob = async { runCatching { getOpticalStores() }.getOrDefault(emptyList()) }
            val apptJob =
                async {
                    session?.let { s ->
                        runCatching { getPatientAppointments(s.patientId) }.getOrDefault(emptyList())
                    }.orEmpty()
                }
            val ordersJob =
                async {
                    session?.let { s ->
                        runCatching { api.getPatientWorkOrders(s.patientId) }.getOrDefault(emptyList())
                    }.orEmpty()
                }
            val recordsJob =
                async {
                    session?.let { s ->
                        runCatching { api.getPatientClinicalRecords(s.patientId) }.getOrDefault(emptyList())
                    }.orEmpty()
                }
            val stores = storesJob.await()
            val storeById = stores.associateBy { it.id }
            val now = Instant.now()
            val apiAppointments =
                apptJob.await().sortedBy { it.startDateTime }.map { appt ->
                    val store = storeById[appt.opticalStoreId]
                    toAppointmentUi(
                        id = appt.id.toString(),
                        start = appt.startDateTime,
                        end = appt.endDateTime,
                        title = "Evaluación visual",
                        storeName = store?.name ?: "Óptica OptiFlow",
                        storeAddress = store?.address ?: "",
                        status = mapStatus(appt.status),
                        isPast = appt.endDateTime.isBefore(now),
                        fromApi = true,
                    ).copy(storeId = appt.opticalStoreId)
                }
            val appointments =
                (apiAppointments.ifEmpty { demoAppointments() })
                    .map { if (it.id in cancelledIds) it.copy(status = AppointmentStatus.CANCELLED) else it }
            val name = session?.name?.takeIf { it.isNotBlank() } ?: "Valentina Silva"
            val records = recordsJob.await()
            _uiState.update {
                it.copy(
                    session = session,
                    stores = stores,
                    appointments = appointments,
                    orders = ordersJob.await().map(::toOrderUi).ifEmpty { demoOrders() },
                    history = records.map(::toHistory).ifEmpty { demoHistory() },
                    prescription = latestPrescription(records, name) ?: demoPrescription(name),
                    isLoading = false,
                )
            }
        }
    }

    fun appointment(id: String): AppointmentUi? = _uiState.value.appointments.firstOrNull { it.id == id }

    fun order(id: String): OrderUi? = _uiState.value.orders.firstOrNull { it.id == id }

    /** Cancela la cita con `PATCH /appointments/{id}/status`; las citas de demostración solo se marcan aquí. */
    fun cancelAppointment(id: String) {
        if (appointment(id)?.fromApi == true) {
            viewModelScope.launch {
                runCatching { api.updateAppointmentStatus(UUID.fromString(id), CANCEL_REQUEST) }
                    .onSuccess { refresh() }
            }
        }
        cancelledIds += id
        _uiState.update { state ->
            state.copy(
                appointments =
                    state.appointments.map {
                        if (it.id == id) it.copy(status = AppointmentStatus.CANCELLED) else it
                    },
            )
        }
    }

    /** Horarios disponibles de una óptica para reprogramar una cita. */
    suspend fun availableSlots(storeId: UUID): List<TimeSlotDto> =
        runCatching { api.getTimeSlots(storeId) }
            .getOrDefault(emptyList())
            .filter { it.status.equals("AVAILABLE", ignoreCase = true) }

    /**
     * Reprograma una cita: primero reserva el nuevo horario y luego cancela la cita anterior, para no
     * perderla si el horario elegido ya no está disponible. [onDone] recibe el error o `null`.
     */
    fun reschedule(appointmentId: String, storeId: UUID, timeSlotId: UUID, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val result =
                runCatching {
                    val session = patientRepository.getSession() ?: error("Inicia sesión para reprogramar tu cita.")
                    api.bookAppointment(BookAppointmentRequest(session.patientId, storeId, timeSlotId))
                    api.updateAppointmentStatus(UUID.fromString(appointmentId), CANCEL_REQUEST)
                }
            onDone(result.exceptionOrNull()?.toUserMessage())
            if (result.isSuccess) refresh()
        }
    }

    fun savePrescription(updated: PrescriptionUi) {
        _uiState.update { it.copy(prescription = updated) }
    }

    fun clearSession(onDone: () -> Unit) {
        viewModelScope.launch {
            patientRepository.clearSession()
            _uiState.value = PatientUiState(isLoading = false)
            onDone()
        }
    }

    private fun mapStatus(raw: String): AppointmentStatus =
        when (raw.uppercase(Locale.US)) {
            "CONFIRMED", "CONFIRMADA", "BOOKED", "SCHEDULED" -> AppointmentStatus.CONFIRMED
            "CANCELLED", "CANCELED", "CANCELADA" -> AppointmentStatus.CANCELLED
            else -> AppointmentStatus.PENDING
        }

    private fun toOrderUi(dto: WorkOrderDto): OrderUi {
        val step =
            when (dto.status.uppercase(Locale.US)) {
                "PENDING" -> 1
                "IN_WORKSHOP", "QUALITY_CONTROL" -> 2
                "READY_FOR_DELIVERY" -> 3
                "DELIVERED" -> 4
                else -> 0
            }
        val created = dto.createdAt ?: Instant.now()
        val estimate =
            dto.estimatedDeliveryDate
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?.let { DateTimeFormatter.ofPattern("d 'de' MMMM", es).format(it) }
                ?: "por confirmar"
        val lens = dto.lenses?.firstOrNull()?.specifications ?: "Lentes monofocales con antirreflejo"
        val history =
            dto.statusHistory.orEmpty().mapNotNull { change -> change.changedAt?.let(::shortDateTime) }
        return OrderUi(
            id = dto.id.toString(),
            code = "OF-" + dto.id.toString().take(4).uppercase(Locale.US),
            title = "Lentes ópticos + montura",
            dateLabel = DateTimeFormatter.ofPattern("d MMM", es).withZone(zone).format(created),
            step = step,
            estimate = estimate,
            product = "Montura Nova N-24 · Azul",
            productDetail = lens,
            pickup = "Óptica Visión Norte · Providencia",
            history = history.ifEmpty { listOf(shortDateTime(created)) },
            fromApi = true,
        )
    }

    private fun toHistory(dto: ClinicalRecordDto): HistoryEntryUi =
        HistoryEntryUi(
            date =
                dto.examinationDate
                    ?.let { DateTimeFormatter.ofPattern("dd MMM yyyy", es).withZone(zone).format(it) }
                    ?.uppercase(es)
                    ?.replace(".", "")
                    ?: "—",
            title = "Evaluación visual",
            summary = dto.observations?.takeIf { it.isNotBlank() } ?: "Sin observaciones",
            professional = "Dra. Fernanda Soto",
        )

    private fun latestPrescription(records: List<ClinicalRecordDto>, name: String): PrescriptionUi? {
        val record =
            records.filter { it.prescription != null }.maxByOrNull { it.examinationDate ?: Instant.EPOCH }
                ?: return null
        val p = record.prescription ?: return null
        val date = record.examinationDate ?: Instant.now()
        return demoPrescription(name).copy(
            date = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(zone).format(date),
            issuedLabel = "Emitida el " + DateTimeFormatter.ofPattern("d MMM yyyy", es).withZone(zone).format(date),
            sphereOD = fmt(p.sphereOD),
            cylinderOD = fmt(p.cylinderOD),
            axisOD = p.axisOD?.let { "$it°" } ?: "—",
            additionOD = fmt(p.addition, plus = true),
            sphereOI = fmt(p.sphereOS),
            cylinderOI = fmt(p.cylinderOS),
            axisOI = p.axisOS?.let { "$it°" } ?: "—",
            additionOI = fmt(p.addition, plus = true),
            observations = p.treatment ?: record.observations.orEmpty(),
        )
    }

    private fun fmt(value: Double?, plus: Boolean = false): String {
        if (value == null) return "—"
        val text = String.format(Locale.US, "%.2f", value)
        return if (plus && value > 0) "+$text" else text.replace("-", "−")
    }

    private fun shortDateTime(instant: Instant): String =
        DateTimeFormatter.ofPattern("d MMM · HH:mm", es).withZone(zone).format(instant)

    private fun toAppointmentUi(
        id: String,
        start: Instant,
        end: Instant,
        title: String,
        storeName: String,
        storeAddress: String,
        status: AppointmentStatus,
        isPast: Boolean,
        fromApi: Boolean,
    ): AppointmentUi {
        val month = DateTimeFormatter.ofPattern("MMM", es).withZone(zone).format(start).replace(".", "")
        return AppointmentUi(
            id = id,
            day = DateTimeFormatter.ofPattern("d").withZone(zone).format(start),
            month = month.uppercase(es),
            monthName =
                DateTimeFormatter.ofPattern("MMMM", es).withZone(zone).format(start)
                    .replaceFirstChar { it.titlecase(es) },
            weekday =
                DateTimeFormatter.ofPattern("EEEE", es).withZone(zone).format(start)
                    .replaceFirstChar { it.titlecase(es) },
            time = DateTimeFormatter.ofPattern("HH:mm").withZone(zone).format(start),
            endTime = DateTimeFormatter.ofPattern("HH:mm").withZone(zone).format(end),
            title = title,
            storeName = storeName,
            storeAddress = storeAddress,
            professional = "Dra. Fernanda Soto",
            status = status,
            isPast = isPast,
            fromApi = fromApi,
        )
    }

    companion object {
        private val CANCEL_REQUEST = UpdateAppointmentStatusRequest(status = "CANCELLED")

        fun demoAppointments(): List<AppointmentUi> =
            listOf(
                AppointmentUi(
                    "demo-1", "24", "JUN", "Junio", "Martes", "10:30", "11:15", "Evaluación visual",
                    "Óptica Visión Norte", "Av. Providencia 1240", "Dra. Fernanda Soto",
                    AppointmentStatus.CONFIRMED, isPast = false, fromApi = false,
                ),
                AppointmentUi(
                    "demo-2", "30", "JUN", "Junio", "Lunes", "16:00", "16:45", "Control de lentes",
                    "Óptica Visión Norte", "Av. Providencia 1240", "Dr. Matías Araya",
                    AppointmentStatus.PENDING, isPast = false, fromApi = false,
                ),
                AppointmentUi(
                    "demo-3", "12", "MAR", "Marzo", "Miércoles", "09:30", "10:15", "Evaluación visual",
                    "Óptica Visión Norte", "Av. Providencia 1240", "Dra. Fernanda Soto",
                    AppointmentStatus.CONFIRMED, isPast = true, fromApi = false,
                ),
            )

        fun demoOrders(): List<OrderUi> =
            listOf(
                OrderUi(
                    "demo-order", "OF-2481", "Lentes ópticos + montura", "14 jun", 2,
                    "26 y 28 de junio", "Montura Nova N-24 · Azul", "Lentes monofocales con antirreflejo",
                    "Óptica Visión Norte · Providencia", listOf("14 jun · 12:24", "14 jun · 12:26"), fromApi = false,
                ),
                OrderUi(
                    "demo-order-2", "OF-2210", "Lentes de sol graduados", "02 mar", 4,
                    "5 de marzo", "Havana Sun · Dorado", "Lentes solares polarizados",
                    "Óptica Visión Norte · Providencia", listOf("2 mar · 10:02", "2 mar · 10:05"), fromApi = false,
                ),
            )

        fun demoHistory(): List<HistoryEntryUi> =
            listOf(
                HistoryEntryUi("12 MAR 2025", "Evaluación visual", "Miopía leve estable", "Dra. Fernanda Soto"),
                HistoryEntryUi("08 SEP 2024", "Control de lentes", "Sin cambios relevantes", "Dr. Matías Araya"),
            )

        fun demoPrescription(name: String): PrescriptionUi =
            PrescriptionUi(
                patientName = name,
                date = "18/10/2026",
                issuedLabel = "Emitida el 18 oct 2026",
                sphereOD = "−1.25", cylinderOD = "−0.50", axisOD = "175°", additionOD = "+0.75",
                sphereOI = "−1.00", cylinderOI = "−0.75", axisOI = "10°", additionOI = "+0.75",
                pupillaryDistance = "62",
                diagnoses = listOf("Miopía", "Astigmatismo"),
                observations = "Uso permanente. Lentes monofocales con tratamiento antirreflejo y filtro UV.",
            )
    }
}
