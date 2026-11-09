package com.logix.optiflow.presentation.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.searchbooking.PatientSession
import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import com.logix.optiflow.domain.usecase.searchbooking.BookAppointmentUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetOpticalStoresUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientSessionUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetStoreAvailabilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val es = Locale("es", "PE")

data class StoreOption(
    val id: UUID?,
    val name: String,
    val address: String,
    val distance: String,
    val availability: String,
    val availableToday: Boolean,
)

data class SlotOption(
    val id: UUID?,
    val date: LocalDate,
    val time: LocalTime,
) {
    val label: String get() = time.format(DateTimeFormatter.ofPattern("HH:mm"))
}

data class BookingUiState(
    val step: Int = 1,
    val stores: List<StoreOption> = emptyList(),
    val selectedStoreIndex: Int = 0,
    val slots: List<SlotOption> = emptyList(),
    val weekStart: LocalDate = LocalDate.now(STORE_ZONE).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val selectedDate: LocalDate? = null,
    val selectedSlot: SlotOption? = null,
    val reason: String = "",
    val session: PatientSession? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val confirmed: Boolean = false,
) {
    val selectedStore: StoreOption? get() = stores.getOrNull(selectedStoreIndex)
    val weekDays: List<LocalDate> get() = (0L..4L).map { weekStart.plusDays(it) }
    val slotsForDate: List<SlotOption> get() = slots.filter { it.date == selectedDate }
    val monthLabel: String
        get() =
            (selectedDate ?: weekStart).format(DateTimeFormatter.ofPattern("MMMM yyyy", es))
                .replaceFirstChar { it.titlecase(es) }
    val patientName: String get() = session?.name?.takeIf { it.isNotBlank() } ?: "Valentina Silva"

    fun hasSlots(date: LocalDate): Boolean = slots.any { it.date == date }
}

/** Flujo de reserva en 4 pasos: óptica → fecha/hora → confirmación → reserva enviada. */
@HiltViewModel
class BookingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOpticalStores: GetOpticalStoresUseCase,
    private val getAvailability: GetStoreAvailabilityUseCase,
    private val bookAppointment: BookAppointmentUseCase,
    private val getPatientSession: GetPatientSessionUseCase,
) : ViewModel() {

    private val preselectedStoreId: UUID? =
        savedStateHandle.get<String>("storeId")?.let { runCatching { UUID.fromString(it) }.getOrNull() }

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val zone = STORE_ZONE

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val session = runCatching { getPatientSession() }.getOrNull()
            val storesResult = getOpticalStores()
            val options =
                storesResult.getOrDefault(emptyList()).mapIndexed { index, store ->
                    val today = index % 2 == 0
                    StoreOption(
                        id = store.id,
                        name = store.name,
                        address = store.address,
                        distance = DISTANCES[index % DISTANCES.size],
                        availability = if (today) "Disponible hoy" else "Disponible mañana",
                        availableToday = today,
                    )
                }
            val preselected = options.indexOfFirst { it.id != null && it.id == preselectedStoreId }
            _uiState.update {
                it.copy(
                    session = session,
                    stores = options,
                    selectedStoreIndex = preselected.coerceAtLeast(0),
                    errorMessage = storesResult.exceptionOrNull()?.toUserMessage(),
                    isLoading = false,
                )
            }
        }
    }

    fun selectStore(index: Int) = _uiState.update { it.copy(selectedStoreIndex = index, errorMessage = null) }

    fun onReasonChange(value: String) = _uiState.update { it.copy(reason = value) }

    fun goToStep(step: Int) = _uiState.update { it.copy(step = step, errorMessage = null) }

    fun back(): Boolean {
        val step = _uiState.value.step
        if (step in 2..3) {
            goToStep(step - 1)
            return true
        }
        return false
    }

    fun continueFromStore() {
        val store = _uiState.value.selectedStore ?: return
        _uiState.update { it.copy(step = 2, isLoading = true, errorMessage = null, selectedSlot = null) }
        viewModelScope.launch {
            val apiSlots =
                store.id?.let { id -> getAvailability(id).getOrDefault(emptyList()) }.orEmpty()
            val slots = apiSlots.map(::toOption)
            val firstDate = slots.minOfOrNull { it.date }
            _uiState.update {
                it.copy(
                    slots = slots,
                    selectedDate = firstDate,
                    weekStart =
                        (firstDate ?: LocalDate.now(STORE_ZONE)).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                    isLoading = false,
                )
            }
        }
    }

    fun shiftWeek(weeks: Long) =
        _uiState.update { state ->
            val start = state.weekStart.plusWeeks(weeks)
            val firstWithSlots = (0L..4L).map { start.plusDays(it) }.firstOrNull(state::hasSlots)
            state.copy(weekStart = start, selectedDate = firstWithSlots ?: start, selectedSlot = null)
        }

    fun selectDate(date: LocalDate) = _uiState.update { it.copy(selectedDate = date, selectedSlot = null) }

    fun selectSlot(slot: SlotOption) = _uiState.update { it.copy(selectedSlot = slot, errorMessage = null) }

    fun continueFromDate() {
        if (_uiState.value.selectedSlot == null) {
            _uiState.update { it.copy(errorMessage = "Selecciona un horario para continuar.") }
            return
        }
        goToStep(3)
    }

    fun confirm(onBooked: () -> Unit) {
        val state = _uiState.value
        val slot = state.selectedSlot ?: return
        val store = state.selectedStore ?: return
        if (slot.id == null || store.id == null) {
            // Sin horarios del backend: se simula la reserva para completar el recorrido.
            goToStep(4)
            return
        }
        if (state.session == null) {
            _uiState.update { it.copy(errorMessage = "Inicia sesión como paciente para reservar.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            bookAppointment(store.id, slot.id)
                .onSuccess { appointment ->
                    _uiState.update {
                        it.copy(isLoading = false, step = 4, confirmed = appointment.status.equals("CONFIRMED", ignoreCase = true))
                    }
                    onBooked()
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
        }
    }

    private fun toOption(slot: TimeSlot): SlotOption {
        val local = slot.startDateTime.atZone(zone)
        return SlotOption(id = slot.id, date = local.toLocalDate(), time = local.toLocalTime())
    }

    companion object {
        private val DISTANCES = listOf("0,8 km", "3,2 km", "1,4 km", "3,6 km")
    }
}
