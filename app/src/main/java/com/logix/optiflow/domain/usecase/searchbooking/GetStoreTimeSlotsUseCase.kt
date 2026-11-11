package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import java.util.UUID
import javax.inject.Inject

/** Todos los turnos publicados de una óptica (libres y reservados), en orden. */
class GetStoreTimeSlotsUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(opticalStoreId: UUID): Result<List<TimeSlot>> =
        repository.getTimeSlots(opticalStoreId).map { slots -> slots.sortedBy { it.startDateTime } }
}
