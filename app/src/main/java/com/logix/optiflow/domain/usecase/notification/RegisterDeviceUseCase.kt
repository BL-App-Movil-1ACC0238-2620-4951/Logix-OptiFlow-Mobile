package com.logix.optiflow.domain.usecase.notification

import com.logix.optiflow.domain.repository.notification.NotificationRepository
import java.util.UUID
import javax.inject.Inject

class RegisterDeviceUseCase @Inject constructor(
    private val repository: NotificationRepository,
) {
    /** Guarda en el backend el token de este celular para que lleguen los push del paciente. */
    suspend operator fun invoke(patientId: UUID): Result<Unit> = repository.registerDevice(patientId)
}
