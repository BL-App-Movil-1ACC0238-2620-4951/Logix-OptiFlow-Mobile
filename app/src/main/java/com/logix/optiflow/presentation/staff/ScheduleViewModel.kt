package com.logix.optiflow.presentation.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import com.logix.optiflow.domain.usecase.searchbooking.GetStoreTimeSlotsUseCase
import com.logix.optiflow.domain.usecase.searchbooking.PublishTimeSlotsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Días que el admin puede abrir, desde hoy. */
const val SCHEDULE_DAYS = 14L

val SLOT_MINUTES = listOf(30L, 45L)

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val day: LocalDate = LocalDate.now(STORE_ZONE),
    val from: String = "09:00",
    val to: String = "13:00",
    val minutes: Long = SLOT_MINUTES.first(),
    val slots: List<TimeSlot> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
) {
    val daySlots: List<TimeSlot> get() = slots.filter { it.startDateTime.atZone(STORE_ZONE).toLocalDate() == day }
}

/** Abre turnos de atención de la óptica del admin (`POST /optical-stores/{id}/time-slots`). */
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getSlots: GetStoreTimeSlotsUseCase,
    private val publish: PublishTimeSlotsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private var storeId: UUID? = null

    fun load(storeId: UUID?) {
        if (storeId == null) return
        this.storeId = storeId
        viewModelScope.launch {
            getSlots(storeId)
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, slots = list) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) } }
        }
    }

    fun selectDay(day: LocalDate) = _uiState.update { it.copy(day = day, message = null, errorMessage = null) }

    fun setFrom(v: String) = _uiState.update { it.copy(from = v.filter { c -> c.isDigit() || c == ':' }.take(5)) }

    fun setTo(v: String) = _uiState.update { it.copy(to = v.filter { c -> c.isDigit() || c == ':' }.take(5)) }

    fun setMinutes(v: Long) = _uiState.update { it.copy(minutes = v) }

    /** Turnos que se crearían con los datos actuales; null si las horas no son válidas. */
    fun preview(state: ScheduleUiState = _uiState.value): Int? {
        val from = parse(state.from) ?: return null
        val to = parse(state.to) ?: return null
        if (!to.isAfter(from)) return null
        return PublishTimeSlotsUseCase.slotsFor(state.day, from, to, state.minutes, STORE_ZONE).size
    }

    fun submit() {
        val s = _uiState.value
        val store = storeId ?: return
        val from = parse(s.from)
        val to = parse(s.to)
        if (from == null || to == null) {
            _uiState.update { it.copy(errorMessage = "Escribe las horas como HH:mm, por ejemplo 09:00.") }
            return
        }
        if (!to.isAfter(from)) {
            _uiState.update { it.copy(errorMessage = "La hora de fin debe ser posterior a la de inicio.") }
            return
        }
        val now = java.time.ZonedDateTime.now(STORE_ZONE)
        if (s.day == now.toLocalDate() && !from.isAfter(now.toLocalTime())) {
            _uiState.update { it.copy(errorMessage = "Hoy solo se pueden abrir turnos que empiecen más tarde.") }
            return
        }
        val overlaps =
            PublishTimeSlotsUseCase.slotsFor(s.day, from, to, s.minutes, STORE_ZONE).any { (start, end) ->
                s.daySlots.any { it.startDateTime < end && start < it.endDateTime }
            }
        if (overlaps) {
            _uiState.update { it.copy(errorMessage = "Ya hay turnos abiertos en ese rango. Elige otro horario.") }
            return
        }
        if (s.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, message = null) }
            publish(store, s.day, from, to, s.minutes, STORE_ZONE)
                .onSuccess { created ->
                    _uiState.update { it.copy(isSaving = false, message = "Se abrieron ${created.size} turnos.") }
                    load(store)
                }
                .onFailure { e ->
                    val msg = if (e is IllegalArgumentException) e.message.orEmpty() else e.toUserMessage()
                    _uiState.update { it.copy(isSaving = false, errorMessage = msg) }
                }
        }
    }

    private fun parse(v: String): LocalTime? =
        runCatching { LocalTime.parse(if (v.length == 4) "0$v" else v) }.getOrNull()
}
