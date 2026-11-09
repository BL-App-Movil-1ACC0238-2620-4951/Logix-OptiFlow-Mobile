package com.logix.optiflow.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Decide la pantalla inicial según la sesión guardada. */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val getPatientSession: GetPatientSessionUseCase,
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

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
