package com.logix.optiflow.presentation.patient

import androidx.lifecycle.ViewModel
import com.logix.optiflow.domain.usecase.notification.RegisterDeviceUseCase
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.clinical.ClinicalRecord
import com.logix.optiflow.domain.model.clinical.OpticalPrescription
import com.logix.optiflow.domain.model.clinical.QuotationItem
import com.logix.optiflow.domain.model.production.WorkOrder
import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.domain.model.searchbooking.PatientSession
import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import com.logix.optiflow.domain.repository.clinical.ClinicalRecordRepository
import com.logix.optiflow.domain.repository.production.WorkOrderRepository
import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import com.logix.optiflow.domain.usecase.clinical.GetPrescriptionUseCase
import com.logix.optiflow.domain.usecase.clinical.GetQuotationUseCase
import com.logix.optiflow.domain.usecase.clinical.GetSaleUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetOpticalStoresUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientAppointmentsUseCase
import com.logix.optiflow.domain.usecase.searchbooking.RateOpticalStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

private val es = Locale("es", "PE")
private val zone: ZoneId = STORE_ZONE

enum class AppointmentStatus(val label: String) {
    CONFIRMED("Confirmada"),
    PENDING("Pendiente"),
    CANCELLED("Cancelada"),
    COMPLETED("Completada"),
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
) {
    /** La cita ya se atendió: el backend la marcó como completada o su horario ya pasó. */
    val attended: Boolean get() = status == AppointmentStatus.COMPLETED || (isPast && status != AppointmentStatus.CANCELLED)
}

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
    /** Fecha y hora en que la orden llegó a cada estado del backend (PENDING, IN_WORKSHOP, …). */
    val statusDates: Map<String, String> = emptyMap(),
    /** Motivo del retraso informado por el taller. */
    val delayReason: String? = null,
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

/** Una receta de la lista "Mis recetas"; [recordId] es la ficha clínica que la contiene. */
data class PrescriptionItemUi(
    val recordId: String,
    val date: String,
    val summary: String,
)

data class PatientUiState(
    val session: PatientSession? = null,
    val appointments: List<AppointmentUi> = emptyList(),
    val orders: List<OrderUi> = emptyList(),
    val history: List<HistoryEntryUi> = emptyList(),
    val prescription: PrescriptionUi? = null,
    val prescriptions: List<PrescriptionItemUi> = emptyList(),
    /** Receta abierta desde "Mis recetas"; null mientras se consulta. */
    val selectedPrescription: PrescriptionUi? = null,
    val selectedPrescriptionError: String? = null,
    val stores: List<OpticalStore> = emptyList(),
    /** Ópticas que el paciente calificó en esta sesión. */
    val ratedStoreIds: Set<UUID> = emptySet(),
    val isLoading: Boolean = true,
) {
    val displayName: String get() = session?.name.orEmpty()
    val firstName: String get() = displayName.trim().substringBefore(' ')
    val email: String get() = session?.email.orEmpty()
    val initials: String
        get() =
            displayName.split(' ').filter { it.isNotBlank() }.take(2)
                .joinToString("") { it.first().uppercase() }
    val upcoming: List<AppointmentUi> get() = appointments.filter { !it.attended && it.status != AppointmentStatus.CANCELLED }
    val nextAppointment: AppointmentUi? get() = upcoming.firstOrNull()
}

/**
 * Estado compartido del paciente (citas, pedidos, historial y receta).
 * Todo sale de la API con la sesión del paciente; sin datos, las pantallas muestran su estado vacío.
 */
@HiltViewModel
class PatientViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val getPatientAppointments: GetPatientAppointmentsUseCase,
    private val getOpticalStores: GetOpticalStoresUseCase,
    private val appointmentRepository: AppointmentRepository,
    private val workOrderRepository: WorkOrderRepository,
    private val clinicalRecordRepository: ClinicalRecordRepository,
    private val rateOpticalStore: RateOpticalStoreUseCase,
    private val getPrescription: GetPrescriptionUseCase,
    private val getSale: GetSaleUseCase,
    private val getQuotation: GetQuotationUseCase,
    private val registerDevice: RegisterDeviceUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientUiState())
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    private val cancelledIds = mutableSetOf<String>()
    private var recordsById: Map<String, ClinicalRecord> = emptyMap()
    private var refreshJob: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            val session = runCatching { patientRepository.getSession() }.getOrNull()
            // Sin internet o sin Google Play no hay push; la app sigue igual.
            session?.takeIf { it.isPatient && !it.token.isNullOrBlank() }?.let { registerDevice(it.patientId) }
        }
    }

    fun refresh() {
        // init y la pantalla de Inicio piden recargar casi a la vez: no repetir las consultas.
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val session = runCatching { patientRepository.getSession() }.getOrNull()
            val storesJob = async { getOpticalStores().getOrDefault(emptyList()) }
            val apptJob =
                async {
                    session?.let { s ->
                        getPatientAppointments(s.patientId).getOrDefault(emptyList())
                    }.orEmpty()
                }
            val ordersJob =
                async {
                    session?.let { s ->
                        workOrderRepository.getPatientWorkOrders(s.patientId).getOrDefault(emptyList())
                    }.orEmpty().let { orders -> orders.zip(productsOf(orders)) }
                }
            val recordsJob =
                async {
                    session?.let { s ->
                        clinicalRecordRepository.getPatientRecords(s.patientId).getOrDefault(emptyList())
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
                apiAppointments
                    .map { if (it.id in cancelledIds) it.copy(status = AppointmentStatus.CANCELLED) else it }
            val name = session?.name.orEmpty()
            val records = recordsJob.await()
            recordsById = records.associateBy { it.id.toString() }
            _uiState.update {
                it.copy(
                    session = session,
                    stores = stores,
                    appointments = appointments,
                    orders = ordersJob.await().map { (order, items) -> toOrderUi(order, items, storeById[order.opticalStoreId]?.name) },
                    history = records.map(::toHistory),
                    prescription = latestPrescription(records, name),
                    prescriptions = prescriptionItems(records),
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
                appointmentRepository.cancelAppointment(UUID.fromString(id))
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
    suspend fun availableSlots(storeId: UUID): List<TimeSlot> =
        appointmentRepository.getAvailability(storeId).getOrDefault(emptyList())

    /**
     * Reprograma una cita: primero reserva el nuevo horario y luego cancela la cita anterior, para no
     * perderla si el horario elegido ya no está disponible. [onDone] recibe el error o `null`.
     */
    fun reschedule(appointmentId: String, storeId: UUID, timeSlotId: UUID, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val result =
                appointmentRepository.bookAppointment(storeId, timeSlotId)
                    .mapCatching { appointmentRepository.cancelAppointment(UUID.fromString(appointmentId)).getOrThrow() }
            onDone(result.exceptionOrNull()?.toUserMessage())
            if (result.isSuccess) refresh()
        }
    }

    /**
     * Califica la óptica de una cita con `POST /optical-stores/{id}/ratings`. El backend acepta una
     * sola calificación por óptica. [onDone] recibe el error o `null`.
     */
    fun rateStore(storeId: UUID, score: Int, comment: String, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val result = rateOpticalStore(storeId, score, comment)
            val error = result.exceptionOrNull()
            onDone(error?.toUserMessage())
            // 409: la óptica ya tenía la calificación de este paciente.
            if (result.isSuccess || (error as? HttpException)?.code() == 409) {
                _uiState.update { it.copy(ratedStoreIds = it.ratedStoreIds + storeId) }
            }
        }
    }

    /** Carga una receta con `GET /clinical-records/{id}/prescription`. */
    fun loadPrescription(recordId: String) {
        val record = recordsById[recordId]
        _uiState.update { it.copy(selectedPrescription = null, selectedPrescriptionError = null) }
        viewModelScope.launch {
            getPrescription(UUID.fromString(recordId))
                .onSuccess { p ->
                    val ui = toPrescriptionUi(p, record?.examinationDate, record?.observations, _uiState.value.displayName)
                    _uiState.update { it.copy(selectedPrescription = ui) }
                }
                .onFailure { error -> _uiState.update { it.copy(selectedPrescriptionError = error.toUserMessage()) } }
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
            "COMPLETED" -> AppointmentStatus.COMPLETED
            else -> AppointmentStatus.PENDING
        }

    /** Montura y lentes de cada orden: salen de su venta (`GET /sales/{id}`) y su cotización. */
    private suspend fun productsOf(orders: List<WorkOrder>): List<List<QuotationItem>> =
        coroutineScope {
            orders.map { order ->
                async {
                    order.saleId
                        ?.let { getSale(it).getOrNull()?.quotationId }
                        ?.let { getQuotation(it).getOrNull()?.items }
                        .orEmpty()
                }
            }.awaitAll()
        }

    private fun toOrderUi(dto: WorkOrder, items: List<QuotationItem>, storeName: String?): OrderUi {
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
        val frame = items.firstOrNull { it.type == "FRAME" }?.description
        val lens =
            items.filter { it.type != "FRAME" }.joinToString(" · ") { it.description }.ifBlank { null }
                ?: dto.lenses.firstOrNull()?.specifications ?: "Lentes"
        val history =
            dto.statusHistory.mapNotNull { change -> change.changedAt?.let(::shortDateTime) }
        return OrderUi(
            id = dto.id.toString(),
            code = "OF-" + dto.id.toString().take(4).uppercase(Locale.US),
            title = if (frame != null) "Lentes ópticos + montura" else "Lentes ópticos",
            dateLabel = DateTimeFormatter.ofPattern("d MMM", es).withZone(zone).format(created),
            step = step,
            estimate = estimate,
            product = frame ?: "Lentes",
            productDetail = lens,
            pickup = storeName ?: "Óptica OptiFlow",
            history = history.ifEmpty { listOf(shortDateTime(created)) },
            fromApi = true,
            statusDates =
                dto.statusHistory
                    .mapNotNull { change -> change.changedAt?.let { change.status to shortDateTime(it) } }
                    .toMap(),
            delayReason = if (dto.delayed) dto.deliveryDelay?.reason else null,
        )
    }

    private fun toHistory(dto: ClinicalRecord): HistoryEntryUi =
        HistoryEntryUi(
            date =
                dto.examinationDate
                    ?.let { DateTimeFormatter.ofPattern("dd MMM yyyy", es).withZone(zone).format(it) }
                    ?.uppercase(es)
                    ?.replace(".", "")
                    ?: "—",
            title = "Evaluación visual",
            summary = dto.observations?.takeIf { it.isNotBlank() } ?: "Sin observaciones",
            professional = "Optometrista de OptiFlow",
        )

    private fun latestPrescription(records: List<ClinicalRecord>, name: String): PrescriptionUi? {
        val record =
            records.filter { it.prescription != null }.maxByOrNull { it.examinationDate ?: Instant.EPOCH }
                ?: return null
        val p = record.prescription ?: return null
        return toPrescriptionUi(p, record.examinationDate, record.observations, name)
    }

    private fun prescriptionItems(records: List<ClinicalRecord>): List<PrescriptionItemUi> {
        return records
            .sortedByDescending { it.examinationDate ?: Instant.EPOCH }
            .mapNotNull { record ->
                val p = record.prescription ?: return@mapNotNull null
                PrescriptionItemUi(
                    recordId = record.id.toString(),
                    date =
                        record.examinationDate
                            ?.let { DateTimeFormatter.ofPattern("d MMM yyyy", es).withZone(zone).format(it) }
                            ?: "Sin fecha",
                    summary = "OD ${fmt(p.sphereOD)} · OI ${fmt(p.sphereOS)}",
                )
            }
    }

    private fun toPrescriptionUi(p: OpticalPrescription, examinationDate: Instant?, observations: String?, name: String): PrescriptionUi {
        val date = examinationDate ?: Instant.now()
        return PrescriptionUi(
            patientName = name,
            pupillaryDistance = "—",
            diagnoses = emptyList(),
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
            observations = p.treatment ?: observations.orEmpty(),
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
            professional = "Optometrista de turno",
            status = status,
            isPast = isPast,
            fromApi = fromApi,
        )
    }
}
