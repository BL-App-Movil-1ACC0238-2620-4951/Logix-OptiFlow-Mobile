package com.logix.optiflow.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.SessionEvents
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** Decide la pantalla inicial según la sesión guardada y cierra la sesión cuando el token vence. */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val getPatientSession: GetPatientSessionUseCase,
    private val patientRepository: PatientRepository,
    sessionEvents: SessionEvents,
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    /** Emite cuando hay que volver al login porque el backend respondió 401. */
    val sessionExpired: Flow<Unit> = sessionEvents.expired.onEach { patientRepository.clearSession() }

    init {
        viewModelScope.launch {
            val session = runCatching { getPatientSession() }.getOrNull()
            _startDestination.value =
                when {
                    session?.token.isNullOrBlank() -> Routes.AUTH
                    session?.isPatient == false -> Routes.S_HOME
                    else -> Routes.P_HOME
                }
        }
    }
}
