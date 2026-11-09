package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import java.util.UUID

class GetStoreAvailabilityUseCase(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(opticalStoreId: UUID): List<TimeSlot> =
        repository.getAvailability(opticalStoreId)
}
