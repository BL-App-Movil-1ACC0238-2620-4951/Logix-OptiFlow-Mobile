package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.Appointment
import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import java.util.UUID

class BookAppointmentUseCase(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(
        opticalStoreId: UUID,
        timeSlotId: UUID,
    ): Appointment = repository.bookAppointment(opticalStoreId, timeSlotId)
}
