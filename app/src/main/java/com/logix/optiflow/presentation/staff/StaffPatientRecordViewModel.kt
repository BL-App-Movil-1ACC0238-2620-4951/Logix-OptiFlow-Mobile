package com.logix.optiflow.presentation.staff

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.clinical.OpticalPrescription
import com.logix.optiflow.domain.model.production.WorkOrderStatus
import com.logix.optiflow.domain.usecase.clinical.GetPatientClinicalRecordsUseCase
import com.logix.optiflow.domain.usecase.production.GetWorkOrdersUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientAppointmentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
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

data class StaffPrescriptionUi(
    val dateLabel: String,
    val rightEye: String,
    val rightDetail: String,
    val leftEye: String,
    val leftDetail: String,
    val note: String?,
)

data class StaffNextAppointmentUi(
    val appointmentId: String,
    val day: String,
    val month: String,
    val statusLabel: String,
    val timeLabel: String,
    /** La cita es de hoy y todavía no terminó: se puede iniciar la atención. */
    val canStartCare: Boolean,
)

/** Orden de trabajo más reciente del paciente para "Proceso de sus lentes". */
data class StaffLensOrderUi(
    val id: String,
    val code: String,
    val statusLabel: String,
    /** Etapas cumplidas de 1 a 5 (Pendiente … Entregada). */
    val step: Int,
    val detail: String,
    val dueLabel: String,
)

data class StaffPatientRecordUiState(
    val isLoading: Boolean = true,
    val lastVisit: String? = null,
    val prescription: StaffPrescriptionUi? = null,
    val nextAppointment: StaffNextAppointmentUi? = null,
    val lensOrder: StaffLensOrderUi? = null,
)

/**
 * Ficha del paciente para el personal: la última receta sale de `GET /patients/{id}/clinical-records`
 * y la próxima cita de `GET /patients/{id}/appointments`.
 */
@HiltViewModel
class StaffPatientRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPatientRecords: GetPatientClinicalRecordsUseCase,
    private val getPatientAppointments: GetPatientAppointmentsUseCase,
    private val getWorkOrders: GetWorkOrdersUseCase,
) : ViewModel() {

    private val patientId: UUID? =
        savedStateHandle.get<String>("id")?.let { runCatching { UUID.fromString(it) }.getOrNull() }

    private val _uiState = MutableStateFlow(StaffPatientRecordUiState())
    val uiState: StateFlow<StaffPatientRecordUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val id = patientId ?: run {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            val recordsCall = async { getPatientRecords(id) }
            val appointmentsCall = async { getPatientAppointments(id) }
            val ordersCall = async { getWorkOrders() }
            val records = recordsCall.await().getOrDefault(emptyList())
            val appointments = appointmentsCall.await().getOrDefault(emptyList())
            val now = Instant.now()
            // La orden en curso; si todas se entregaron, la última.
            val orders = ordersCall.await().getOrDefault(emptyList()).filter { it.patientId == id }
            val order =
                orders.filter { it.status != WorkOrderStatus.DELIVERED }.maxByOrNull { it.createdAt ?: Instant.EPOCH }
                    ?: orders.maxByOrNull { it.createdAt ?: Instant.EPOCH }

            val examined = records.filter { it.status != "PENDING" && it.examinationDate != null }
            val lastVisit = examined.maxByOrNull { it.examinationDate!! }?.examinationDate
            val latest =
                records.filter { it.prescription != null }.maxByOrNull { it.examinationDate ?: Instant.EPOCH }
            val next =
                appointments
                    .filter { (it.status == "CONFIRMED" || it.status == "PENDING") && it.endDateTime.isAfter(now) }
                    .minByOrNull { it.startDateTime }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    lastVisit = lastVisit?.let { date -> DATE_FORMAT.format(date.atZone(STORE_ZONE)) },
                    lensOrder = order?.let { o ->
                        StaffLensOrderUi(
                            id = o.id.toString(),
                            code = "OT-" + o.id.toString().take(4).uppercase(Locale.US),
                            statusLabel = workOrderStatusLabel(o.status),
                            step = WorkOrderStatus.ORDER.indexOf(o.status) + 1,
                            detail = o.lenses.firstOrNull()?.specifications?.substringAfter("| ", "")?.ifBlank { null }
                                ?.let { t -> "${o.lenses.size} lentes · $t" } ?: "${o.lenses.size} lentes",
                            dueLabel =
                                if (o.status == WorkOrderStatus.DELIVERED) "Entregada"
                                else "Entrega estimada: ${workOrderDate(o.estimatedDeliveryDate) ?: "sin fecha"}" +
                                    if (o.delayed) " (atrasada)" else "",
                        )
                    },
                    prescription = latest?.let { record ->
                        record.prescription!!.toUi(record.examinationDate)
                    },
                    nextAppointment = next?.let { appointment ->
                        val start = appointment.startDateTime.atZone(STORE_ZONE)
                        StaffNextAppointmentUi(
                            appointmentId = appointment.id.toString(),
                            day = start.dayOfMonth.toString(),
                            month = MONTH_FORMAT.format(start).replace(".", "").uppercase(ES),
                            statusLabel = if (appointment.status == "PENDING") "Pendiente" else "Confirmada",
                            timeLabel = TIME_FORMAT.format(start) + " · Evaluación visual",
                            canStartCare = start.toLocalDate() == LocalDate.now(STORE_ZONE),
                        )
                    },
                )
            }
        }
    }

    private fun OpticalPrescription.toUi(date: Instant?): StaffPrescriptionUi =
        StaffPrescriptionUi(
            dateLabel = date?.let { DATE_FORMAT.format(it.atZone(STORE_ZONE)) } ?: "Sin fecha",
            rightEye = "${sphereOD.diopters()} / ${cylinderOD.diopters()}",
            rightDetail = "Eje ${axisOD ?: 0}°",
            leftEye = "${sphereOS.diopters()} / ${cylinderOS.diopters()}",
            leftDetail = "Eje ${axisOS ?: 0}°",
            note =
                listOfNotNull(
                    addition?.let { "Adición +${String.format(Locale.US, "%.2f", it)}" },
                    treatment,
                    recommendedFrameType?.let { "Montura: $it" },
                ).joinToString(" · ").ifBlank { null },
        )

    private fun Double?.diopters(): String {
        val value = this ?: 0.0
        return (if (value > 0) "+" else "") + String.format(Locale.US, "%.2f", value)
    }

    private companion object {
        val ES = Locale("es", "PE")
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", ES)
        val MONTH_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", ES)
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
