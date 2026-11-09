package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import java.util.UUID
import javax.inject.Inject

class GetStoreAvailabilityUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(opticalStoreId: UUID): Result<List<TimeSlot>> =
        repository.getAvailability(opticalStoreId)
}
