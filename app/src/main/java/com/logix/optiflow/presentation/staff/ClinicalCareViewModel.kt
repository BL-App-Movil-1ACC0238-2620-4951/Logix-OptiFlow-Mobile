package com.logix.optiflow.presentation.staff

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.clinical.ClinicalRecord
import com.logix.optiflow.domain.model.clinical.MedicalHistory
import com.logix.optiflow.domain.model.clinical.OpticalPrescription
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import com.logix.optiflow.domain.usecase.clinical.GeneratePrescriptionUseCase
import com.logix.optiflow.domain.usecase.clinical.GetPatientClinicalRecordsUseCase
import com.logix.optiflow.domain.usecase.clinical.RecordMedicalHistoryUseCase
import com.logix.optiflow.domain.usecase.clinical.RegisterExaminationUseCase
import com.logix.optiflow.domain.usecase.searchbooking.CompleteAppointmentUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientAppointmentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
import retrofit2.HttpException

enum class CareStep { EXAMINATION, HISTORY, PRESCRIPTION, DONE }

/** Campos de la receta tal como los escribe el optometrista. */
data class PrescriptionForm(
    val sphereOD: String = "",
    val cylinderOD: String = "",
    val axisOD: String = "",
    val sphereOS: String = "",
    val cylinderOS: String = "",
    val axisOS: String = "",
    val addition: String = "",
    val treatment: String = "",
    val recommendedFrameType: String = "",
)

data class ClinicalCareUiState(
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val patientName: String = "",
    val appointmentLabel: String = "",
    val step: CareStep = CareStep.EXAMINATION,
    val observations: String = "",
    val allergies: String = "",
    val previousConditions: String = "",
    val familyHistory: String = "",
    val prescription: PrescriptionForm = PrescriptionForm(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * Atención clínica de una cita en 3 pasos: `POST /clinical-records` (examen),
 * `PUT /clinical-records/{id}/medical-history` (antecedentes) y
 * `POST /clinical-records/{id}/prescription` (receta). Al guardar la receta la cita pasa a COMPLETED.
 * Si la ficha de la cita ya estaba abierta, la atención continúa desde donde quedó.
 */
@HiltViewModel
class ClinicalCareViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val patientRepository: PatientRepository,
    private val getPatientAppointments: GetPatientAppointmentsUseCase,
    private val getPatientRecords: GetPatientClinicalRecordsUseCase,
    private val registerExamination: RegisterExaminationUseCase,
    private val recordMedicalHistory: RecordMedicalHistoryUseCase,
    private val generatePrescription: GeneratePrescriptionUseCase,
    private val completeAppointment: CompleteAppointmentUseCase,
) : ViewModel() {

    private val appointmentId: UUID? = savedStateHandle.get<String>("appointmentId")?.toUuid()
    private val patientId: UUID? = savedStateHandle.get<String>("patientId")?.toUuid()

    private val _uiState = MutableStateFlow(ClinicalCareUiState())
    val uiState: StateFlow<ClinicalCareUiState> = _uiState.asStateFlow()

    private var recordId: UUID? = null

    // La receta ya se guardó pero falló completar la cita: el reintento solo completa la cita.
    private var prescriptionSaved = false

    init {
        load()
    }

    fun load() {
        val appointment = appointmentId
        val patient = patientId
        if (appointment == null || patient == null) {
            _uiState.update { it.copy(isLoading = false, loadError = "No se encontró la cita.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            val name = patientRepository.getPatient(patient).getOrNull()?.name ?: "Paciente"
            val found = getPatientAppointments(patient).getOrNull()?.firstOrNull { it.id == appointment }
            val records = getPatientRecords(patient)
            if (records.isFailure) {
                _uiState.update {
                    it.copy(isLoading = false, loadError = records.exceptionOrNull()?.toUserMessage())
                }
                return@launch
            }
            val label =
                found?.let {
                    val start = it.startDateTime.atZone(STORE_ZONE)
                    val day =
                        if (start.toLocalDate() == LocalDate.now(STORE_ZONE)) "Hoy" else DAY_FORMAT.format(start)
                    "$day · ${TIME_FORMAT.format(start)}"
                }.orEmpty()
            _uiState.update { it.copy(isLoading = false, patientName = name, appointmentLabel = label) }
            if (found?.status == "CANCELLED") {
                _uiState.update { it.copy(loadError = "La cita fue cancelada; no se puede atender.") }
                return@launch
            }
            records.getOrNull()?.firstOrNull { it.appointmentId == appointment }?.let { record ->
                resume(record, completed = found?.status == "COMPLETED")
            }
        }
    }

    /** Continúa una ficha ya registrada: rellena lo guardado y salta al paso que falta. */
    private fun resume(record: ClinicalRecord, completed: Boolean) {
        if (record.status == "PENDING") return
        recordId = record.id
        val history = record.medicalHistory
        val rx = record.prescription
        prescriptionSaved = rx != null
        _uiState.update { state ->
            state.copy(
                observations = record.observations.orEmpty(),
                allergies = history?.allergies?.joinToString(", ").orEmpty(),
                previousConditions = history?.previousConditions?.joinToString(", ").orEmpty(),
                familyHistory = history?.familyOcularHistory.orEmpty(),
                prescription = rx?.toForm() ?: state.prescription,
                step =
                    when {
                        rx != null && completed -> CareStep.DONE
                        rx != null || history != null -> CareStep.PRESCRIPTION
                        else -> CareStep.HISTORY
                    },
            )
        }
    }

    fun onObservationsChange(value: String) {
        if (value.length <= MAX_OBSERVATIONS) _uiState.update { it.copy(observations = value) }
    }

    fun onAllergiesChange(value: String) = _uiState.update { it.copy(allergies = value) }

    fun onPreviousConditionsChange(value: String) = _uiState.update { it.copy(previousConditions = value) }

    fun onFamilyHistoryChange(value: String) {
        if (value.length <= MAX_FAMILY_HISTORY) _uiState.update { it.copy(familyHistory = value) }
    }

    fun onPrescriptionChange(form: PrescriptionForm) = _uiState.update { it.copy(prescription = form) }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    /** Vuelve al paso anterior para revisar; el examen ya registrado no se puede volver a enviar. */
    fun back(): Boolean {
        val step = _uiState.value.step
        if (step != CareStep.PRESCRIPTION) return false
        _uiState.update { it.copy(step = CareStep.HISTORY, errorMessage = null) }
        return true
    }

    fun saveExamination() {
        val appointment = appointmentId ?: return
        val patient = patientId ?: return
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            registerExamination(patient, appointment, _uiState.value.observations)
                .onSuccess { record ->
                    recordId = record.id
                    _uiState.update { it.copy(isSaving = false, step = CareStep.HISTORY) }
                }
                .onFailure { error ->
                    // 409: la ficha ya estaba registrada (por ejemplo, desde otro dispositivo).
                    val existing =
                        if (error is HttpException && error.code() == 409) {
                            getPatientRecords(patient).getOrNull()
                                ?.firstOrNull { it.appointmentId == appointment && it.status != "PENDING" }
                        } else {
                            null
                        }
                    _uiState.update { it.copy(isSaving = false) }
                    if (existing != null) {
                        resume(existing, completed = false)
                    } else {
                        _uiState.update { it.copy(errorMessage = error.toUserMessage()) }
                    }
                }
        }
    }

    fun saveHistory() {
        val id = recordId ?: return
        if (_uiState.value.isSaving) return
        val state = _uiState.value
        val allergies = state.allergies.toItems()
        val conditions = state.previousConditions.toItems()
        val family = state.familyHistory.trim().ifBlank { null }
        val invalid =
            listOf(allergies, conditions).firstNotNullOfOrNull { items ->
                when {
                    items.size > MAX_ITEMS -> "Ingresa como máximo $MAX_ITEMS elementos por campo."
                    items.any { it.length > MAX_ITEM_LENGTH } -> "Cada elemento puede tener hasta $MAX_ITEM_LENGTH caracteres."
                    else -> null
                }
            }
        if (invalid != null) {
            _uiState.update { it.copy(errorMessage = invalid) }
            return
        }
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            recordMedicalHistory(id, MedicalHistory(allergies, conditions, family))
                .onSuccess { _uiState.update { it.copy(isSaving = false, step = CareStep.PRESCRIPTION) } }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.toUserMessage()) } }
        }
    }

    fun savePrescriptionAndFinish(onCompleted: () -> Unit) {
        val id = recordId ?: return
        val appointment = appointmentId ?: return
        if (_uiState.value.isSaving) return
        val prescription =
            if (prescriptionSaved) {
                null
            } else {
                _uiState.value.prescription.toPrescription().getOrElse { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                    return
                }
            }
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            if (prescription != null) {
                val saved = generatePrescription(id, prescription)
                if (saved.isFailure) {
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = saved.exceptionOrNull()?.toUserMessage())
                    }
                    return@launch
                }
                prescriptionSaved = true
            }
            completeAppointment(appointment)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, step = CareStep.DONE) }
                    onCompleted()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "La receta se guardó, pero no se pudo cerrar la cita: ${error.toUserMessage()}",
                        )
                    }
                }
        }
    }

    private companion object {
        const val MAX_OBSERVATIONS = 500
        const val MAX_FAMILY_HISTORY = 1000
        const val MAX_ITEMS = 20
        const val MAX_ITEM_LENGTH = 100
        val ES = Locale("es", "PE")
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", ES)
    }
}

private fun String.cap(): String = replaceFirstChar { it.uppercase() }

private fun String.toUuid(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()

/** "Polen, Penicilina" → ["Polen", "Penicilina"]. */
private fun String.toItems(): List<String> =
    split(',', ';', '\n').map { it.trim() }.filter { it.isNotEmpty() }.distinct()

private fun OpticalPrescription.toForm(): PrescriptionForm =
    PrescriptionForm(
        sphereOD = sphereOD.formatDiopters(),
        cylinderOD = cylinderOD.formatDiopters(),
        axisOD = axisOD?.toString().orEmpty(),
        sphereOS = sphereOS.formatDiopters(),
        cylinderOS = cylinderOS.formatDiopters(),
        axisOS = axisOS?.toString().orEmpty(),
        addition = addition.formatDiopters(),
        treatment = treatment.orEmpty(),
        recommendedFrameType = recommendedFrameType.orEmpty(),
    )

private fun Double?.formatDiopters(): String = this?.let { String.format(Locale.US, "%.2f", it) }.orEmpty()

/** Valida con los mismos rangos del backend: esfera ±30, cilindro ±10, eje 0–180 y adición 0–4. */
private fun PrescriptionForm.toPrescription(): Result<OpticalPrescription> =
    runCatching {
        fun diopters(value: String, label: String, limit: Double): Double {
            val number =
                value.trim().replace(',', '.').removePrefix("+").toDoubleOrNull()
                    ?: error("Ingresa $label.")
            require(number in -limit..limit) { "${label.cap()} debe estar entre -${limit.toInt()} y ${limit.toInt()}." }
            return number
        }

        fun axis(value: String, label: String): Int {
            val number = value.trim().toIntOrNull() ?: error("Ingresa $label.")
            require(number in 0..180) { "${label.cap()} debe estar entre 0 y 180 grados." }
            return number
        }
        val add =
            addition.trim().takeIf { it.isNotEmpty() }?.let {
                val number = it.replace(',', '.').removePrefix("+").toDoubleOrNull() ?: error("La adición no es válida.")
                require(number in 0.0..4.0) { "La adición debe estar entre 0 y 4." }
                number
            }
        require(treatment.length <= 255 && recommendedFrameType.length <= 255) {
            "El tratamiento y la montura pueden tener hasta 255 caracteres."
        }
        OpticalPrescription(
            sphereOD = diopters(sphereOD, "la esfera del OD", 30.0),
            cylinderOD = diopters(cylinderOD, "el cilindro del OD", 10.0),
            axisOD = axis(axisOD, "el eje del OD"),
            sphereOS = diopters(sphereOS, "la esfera del OI", 30.0),
            cylinderOS = diopters(cylinderOS, "el cilindro del OI", 10.0),
            axisOS = axis(axisOS, "el eje del OI"),
            addition = add,
            treatment = treatment.trim().ifBlank { null },
            recommendedFrameType = recommendedFrameType.trim().ifBlank { null },
        )
    }
