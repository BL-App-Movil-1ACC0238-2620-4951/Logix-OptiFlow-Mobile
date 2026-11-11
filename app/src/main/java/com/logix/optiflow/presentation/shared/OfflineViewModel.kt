package com.logix.optiflow.presentation.shared

import androidx.lifecycle.ViewModel
import com.logix.optiflow.domain.usecase.offline.ObserveSavedDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/** Expone si la pantalla muestra datos guardados por falta de conexión (US20). */
@HiltViewModel
class OfflineViewModel @Inject constructor(observeSavedData: ObserveSavedDataUseCase) : ViewModel() {
    val savedDataFrom: StateFlow<Instant?> = observeSavedData()
}
