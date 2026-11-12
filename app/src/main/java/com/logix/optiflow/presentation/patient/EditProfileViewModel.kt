package com.logix.optiflow.presentation.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    /** Fecha en formato dd/mm/aaaa; vacía si el paciente no la registró. */
    val birthDate: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

/** El paciente edita su nombre, teléfono y fecha de nacimiento con `PATCH /patients/{id}`. */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val session = patientRepository.getSession()
            if (session == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Inicia sesión para editar tu perfil.") }
                return@launch
            }
            patientRepository.getPatient(session.patientId)
                .onSuccess { p ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = p.name,
                            email = p.email,
                            phone = p.phone,
                            birthDate = p.birthDate?.let(::toDisplayDate).orEmpty(),
                        )
                    }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, name = session.name, email = session.email, errorMessage = e.toUserMessage())
                    }
                }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }

    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value, errorMessage = null) }

    fun onBirthDateChange(value: String) = _uiState.update { it.copy(birthDate = value, errorMessage = null) }

    fun save() {
        val s = _uiState.value
        if (s.isSaving) return
        val name = s.name.trim()
        val phone = s.phone.trim()
        if (name.length < 2) return _uiState.update { it.copy(errorMessage = "Escribe tu nombre completo.") }
        if (phone.isEmpty()) return _uiState.update { it.copy(errorMessage = "Escribe tu teléfono.") }
        val birthDate =
            if (s.birthDate.isBlank()) {
                null
            } else {
                parseBirthDate(s.birthDate)
                    ?: return _uiState.update { it.copy(errorMessage = "Escribe la fecha como 24/05/1990.") }
            }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            patientRepository.updateProfile(name, phone, birthDate)
                .onSuccess { _uiState.update { it.copy(isSaving = false, saved = true) } }
                .onFailure { e -> _uiState.update { it.copy(isSaving = false, errorMessage = e.toUserMessage()) } }
        }
    }
}

private val DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu")

/** "1990-05-24" → "24/05/1990". */
private fun toDisplayDate(iso: String): String =
    runCatching { LocalDate.parse(iso).format(DISPLAY_DATE) }.getOrDefault(iso)

/** "24/05/1990" → "1990-05-24"; null si no es una fecha pasada válida. */
private fun parseBirthDate(text: String): String? =
    runCatching {
        LocalDate.parse(text.trim(), DateTimeFormatter.ofPattern("d/M/uuuu"))
    }.getOrNull()?.takeIf { it.isBefore(LocalDate.now(STORE_ZONE)) }?.toString()
