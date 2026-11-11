package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import java.util.UUID
import javax.inject.Inject

class CompleteAppointmentUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(appointmentId: UUID): Result<Unit> =
        repository.completeAppointment(appointmentId)
}
