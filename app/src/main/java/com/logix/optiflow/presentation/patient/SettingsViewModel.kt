package com.logix.optiflow.presentation.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.local.AppPreferencesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Guarda en el celular el interruptor de recordatorios de Configuración. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: AppPreferencesStore,
) : ViewModel() {

    val checkupReminders: StateFlow<Boolean> =
        preferences.checkupReminders.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggleCheckupReminders() {
        viewModelScope.launch { preferences.setCheckupReminders(!checkupReminders.value) }
    }
}
