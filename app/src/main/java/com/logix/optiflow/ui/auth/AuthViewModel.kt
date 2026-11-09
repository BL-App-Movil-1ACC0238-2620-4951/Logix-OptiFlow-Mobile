package com.logix.optiflow.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.PatientSession
import com.logix.optiflow.domain.usecase.GetPatientSessionUseCase
import com.logix.optiflow.domain.usecase.LoginUseCase
import com.logix.optiflow.domain.usecase.RegisterPatientUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UserRole {
    PATIENT,
    CLINICAL,
}

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val isRegisterMode: Boolean = false,
    val role: UserRole = UserRole.PATIENT,
    val passwordVisible: Boolean = false,
    val acceptedTerms: Boolean = false,
    val session: PatientSession? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authenticatedAs: UserRole? = null,
)

class AuthViewModel(
    private val registerPatient: RegisterPatientUseCase,
    private val login: LoginUseCase,
    private val getPatientSession: GetPatientSessionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(session = getPatientSession()) }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun toggleTerms() = _uiState.update { it.copy(acceptedTerms = !it.acceptedTerms, errorMessage = null) }

    fun setRegisterMode(register: Boolean) =
        _uiState.update { it.copy(isRegisterMode = register, errorMessage = null) }

    fun setRole(role: UserRole) = _uiState.update { it.copy(role = role, errorMessage = null) }

    fun togglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun consumeAuthenticated() = _uiState.update { it.copy(authenticatedAs = null) }

    fun submit() {
        val state = _uiState.value
        val error = validate(state)
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }
        if (state.isRegisterMode) register() else signIn()
    }

    private fun validate(state: AuthUiState): String? {
        if (state.isRegisterMode && state.role == UserRole.CLINICAL) {
            return "Las cuentas del personal las crea el administrador de la óptica."
        }
        if (state.isRegisterMode && state.name.isBlank()) return "Ingresa tu nombre completo."
        if (!state.email.contains('@')) return "Ingresa un correo válido."
        if (state.isRegisterMode && state.role == UserRole.PATIENT && state.phone.isBlank()) {
            return "Ingresa tu teléfono."
        }
        if (state.password.length < MIN_PASSWORD) return "La contraseña debe tener al menos $MIN_PASSWORD caracteres."
        if (state.isRegisterMode && !state.acceptedTerms) return "Acepta los Términos de Servicio para continuar."
        return null
    }

    private fun register() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val state = _uiState.value
                val email = state.email.trim()
                registerPatient(
                    name = state.name.trim(),
                    email = email,
                    phone = state.phone.trim(),
                    password = state.password,
                )
                val session = login(email = email, password = state.password)
                _uiState.update {
                    it.copy(isLoading = false, session = session, authenticatedAs = UserRole.PATIENT)
                }
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
            }
        }
    }

    /** Abre el inicio del paciente o del personal según el rol de la cuenta. */
    private fun signIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val session = login(email = _uiState.value.email.trim(), password = _uiState.value.password)
                val role = if (session.isPatient) UserRole.PATIENT else UserRole.CLINICAL
                _uiState.update {
                    it.copy(isLoading = false, session = session, authenticatedAs = role)
                }
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
            }
        }
    }

    companion object {
        /** El backend exige 8 caracteres como mínimo para la contraseña del paciente. */
        const val MIN_PASSWORD = 8
    }
}
