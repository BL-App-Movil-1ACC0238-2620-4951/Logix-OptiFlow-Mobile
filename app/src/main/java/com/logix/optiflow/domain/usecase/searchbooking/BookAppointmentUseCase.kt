package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.Appointment
import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import java.util.UUID
import javax.inject.Inject

class BookAppointmentUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(
        opticalStoreId: UUID,
        timeSlotId: UUID,
    ): Result<Appointment> = repository.bookAppointment(opticalStoreId, timeSlotId)
}
