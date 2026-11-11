package com.logix.optiflow.domain.usecase.offline

import com.logix.optiflow.domain.repository.offline.OfflineDataRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/** Fecha de los datos guardados que se muestran sin conexión, o null si están al día. */
class ObserveSavedDataUseCase @Inject constructor(
    private val repository: OfflineDataRepository,
) {
    operator fun invoke(): StateFlow<Instant?> = repository.showingSavedDataFrom
}
