package com.logix.optiflow.domain.repository.offline

import java.time.Instant
import kotlinx.coroutines.flow.StateFlow

/** Datos guardados en el dispositivo para consultarlos sin conexión (US20). */
interface OfflineDataRepository {

    /** Fecha de los datos guardados que se muestran por falta de conexión, o null si los datos están al día. */
    val showingSavedDataFrom: StateFlow<Instant?>

    /** Borra lo guardado, por ejemplo al cerrar sesión. */
    suspend fun clear()
}
