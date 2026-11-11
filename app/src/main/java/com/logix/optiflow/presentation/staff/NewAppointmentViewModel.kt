package com.logix.optiflow.presentation.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import com.logix.optiflow.domain.usecase.searchbooking.BookAppointmentUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetStoreTimeSlotsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewAppointmentUiState(
    val isLoading: Boolean = true,
    val patientId: String? = null,
    /** Turnos libres y futuros de la óptica. */
    val slots: List<TimeSlot> = emptyList(),
    val day: LocalDate? = null,
    val slotId: UUID? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val booked: Boolean = false,
) {
    val days: List<LocalDate> get() = slots.map { it.startDateTime.atZone(STORE_ZONE).toLocalDate() }.distinct()
    val daySlots: List<TimeSlot> get() = slots.filter { it.startDateTime.atZone(STORE_ZONE).toLocalDate() == day }
}

/** El personal reserva una cita para un paciente en un turno libre de su óptica (`POST /appointments`). */
@HiltViewModel
class NewAppointmentViewModel @Inject constructor(
    private val getSlots: GetStoreTimeSlotsUseCase,
    private val bookAppointment: BookAppointmentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewAppointmentUiState())
    val uiState: StateFlow<NewAppointmentUiState> = _uiState.asStateFlow()

    private var storeId: UUID? = null

    fun load(storeId: UUID?, patientId: String?) {
        if (storeId == null || this.storeId == storeId) return
        this.storeId = storeId
        _uiState.update { it.copy(patientId = it.patientId ?: patientId) }
        reloadSlots(storeId)
    }

    private fun reloadSlots(storeId: UUID) {
        viewModelScope.launch {
            val now = Instant.now()
            getSlots(storeId)
                .onSuccess { list ->
                    val free = list.filter { it.status == "AVAILABLE" && it.startDateTime.isAfter(now) }
                    _uiState.update { s ->
                        val days = free.map { it.startDateTime.atZone(STORE_ZONE).toLocalDate() }
                        s.copy(isLoading = false, slots = free, day = s.day?.takeIf { it in days } ?: days.firstOrNull())
                    }
                }.onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) } }
        }
    }

    fun selectPatient(id: String) = _uiState.update { it.copy(patientId = id, errorMessage = null) }

    fun selectDay(day: LocalDate) = _uiState.update { it.copy(day = day, slotId = null, errorMessage = null) }

    fun selectSlot(id: UUID) = _uiState.update { it.copy(slotId = id, errorMessage = null) }

    fun submit() {
        val s = _uiState.value
        val store = storeId ?: return
        val patient = s.patientId?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        val slot = s.slotId
        if (patient == null) return _uiState.update { it.copy(errorMessage = "Elige al paciente.") }
        if (slot == null) return _uiState.update { it.copy(errorMessage = "Elige un horario.") }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            bookAppointment(store, slot, patient)
                .onSuccess { _uiState.update { it.copy(isSaving = false, booked = true) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, slotId = null, errorMessage = e.toUserMessage()) }
                    // El turno pudo ocuparse mientras tanto: se vuelve a leer.
                    reloadSlots(store)
                }
        }
    }
}
